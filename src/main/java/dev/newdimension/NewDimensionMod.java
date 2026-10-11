package dev.newdimension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class NewDimensionMod implements ModInitializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, DimensionSpec>>() {}.getType();
    private static final Map<String, DimensionSpec> SPECS = new LinkedHashMap<>();
    private static Path configPath;

    private record DimensionSpec(String id, String surfaceBlock, String portalFrameBlock,
                                 String gamemode, String inventory) {}

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> load(server));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> save());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal("newdimension")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("create")
                    .then(Commands.argument("id", StringArgumentType.word())
                    .then(Commands.argument("surface_block", StringArgumentType.word())
                    .then(Commands.argument("portal_frame_block", StringArgumentType.word())
                    .then(Commands.argument("gamemode", StringArgumentType.word())
                    .then(Commands.argument("inventory", StringArgumentType.word())
                    .executes(ctx -> {
                        String id = cleanId(StringArgumentType.getString(ctx, "id"));
                        String surface = cleanBlock(StringArgumentType.getString(ctx, "surface_block"));
                        String frame = cleanBlock(StringArgumentType.getString(ctx, "portal_frame_block"));
                        String mode = StringArgumentType.getString(ctx, "gamemode").toLowerCase(Locale.ROOT);
                        String inventory = StringArgumentType.getString(ctx, "inventory").toLowerCase(Locale.ROOT);

                        if (!id.matches("[a-z0-9_-]{1,48}")) {
                            ctx.getSource().sendFailure(Component.literal("Invalid id. Use lowercase letters, numbers, _ or - (max 48)."));
                            return 0;
                        }
                        if (!validBlockId(surface) || !validBlockId(frame)) {
                            ctx.getSource().sendFailure(Component.literal("Block IDs must look like minecraft:stone."));
                            return 0;
                        }
                        if (!mode.equals("survival") && !mode.equals("creative") &&
                            !mode.equals("adventure") && !mode.equals("spectator")) {
                            ctx.getSource().sendFailure(Component.literal("Gamemode must be survival, creative, adventure, or spectator."));
                            return 0;
                        }
                        if (!inventory.equals("shared") && !inventory.equals("separate")) {
                            ctx.getSource().sendFailure(Component.literal("Inventory must be shared or separate."));
                            return 0;
                        }
                        if (SPECS.containsKey(id)) {
                            ctx.getSource().sendFailure(Component.literal("A definition named '" + id + "' already exists."));
                            return 0;
                        }
                        SPECS.put(id, new DimensionSpec(id, surface, frame, mode, inventory));
                        save();
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "Saved dimension definition '" + id + "'. Surface=" + surface +
                            ", portal frame=" + frame + ", gamemode=" + mode + ", inventory=" + inventory + "."
                        ), true);
                        ctx.getSource().sendFailure(Component.literal(
                            "Not a playable dimension yet: runtime dimension registration, terrain generation, portals and inventory switching are not included in this build."
                        ));
                        return 1;
                    })))))))
                .then(Commands.literal("list").executes(ctx -> {
                    if (SPECS.isEmpty()) {
                        ctx.getSource().sendSuccess(() -> Component.literal("No saved dimension definitions."), false);
                    } else {
                        ctx.getSource().sendSuccess(() -> Component.literal("Saved definitions: " + String.join(", ", SPECS.keySet())), false);
                    }
                    return 1;
                }))
                .then(Commands.literal("info")
                    .then(Commands.argument("id", StringArgumentType.word()).executes(ctx -> {
                        String id = cleanId(StringArgumentType.getString(ctx, "id"));
                        DimensionSpec spec = SPECS.get(id);
                        if (spec == null) {
                            ctx.getSource().sendFailure(Component.literal("No definition named '" + id + "'."));
                            return 0;
                        }
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            spec.id() + ": surface=" + spec.surfaceBlock() +
                            ", portal=" + spec.portalFrameBlock() + ", gamemode=" + spec.gamemode() +
                            ", inventory=" + spec.inventory()
                        ), false);
                        return 1;
                    })))
                .then(Commands.literal("remove")
                    .then(Commands.argument("id", StringArgumentType.word()).executes(ctx -> {
                        String id = cleanId(StringArgumentType.getString(ctx, "id"));
                        if (SPECS.remove(id) == null) {
                            ctx.getSource().sendFailure(Component.literal("No definition named '" + id + "'."));
                            return 0;
                        }
                        save();
                        ctx.getSource().sendSuccess(() -> Component.literal("Removed saved definition '" + id + "'."), true);
                        ctx.getSource().sendFailure(Component.literal("This only removes the saved definition; no dimension world data is deleted."));
                        return 1;
                    })))
            )
        );
    }

    private static String cleanId(String s) { return s.toLowerCase(Locale.ROOT); }

    private static String cleanBlock(String s) {
        String v = s.toLowerCase(Locale.ROOT);
        return v.contains(":") ? v : "minecraft:" + v;
    }

    private static boolean validBlockId(String s) {
        return s.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }

    private static void load(MinecraftServer server) {
        configPath = server.getWorldPath(LevelResource.ROOT).resolve("new-dimensions.json");
        SPECS.clear();
        if (!Files.exists(configPath)) return;
        try (Reader reader = Files.newBufferedReader(configPath)) {
            Map<String, DimensionSpec> loaded = GSON.fromJson(reader, MAP_TYPE);
            if (loaded != null) SPECS.putAll(loaded);
        } catch (Exception e) {
            server.getLogger().error("[New Dimension] Could not load new-dimensions.json", e);
        }
    }

    private static void save() {
        if (configPath == null) return;
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(SPECS, MAP_TYPE, writer);
            }
        } catch (Exception e) {
            System.err.println("[New Dimension] Could not save definitions: " + e.getMessage());
        }
    }
}
