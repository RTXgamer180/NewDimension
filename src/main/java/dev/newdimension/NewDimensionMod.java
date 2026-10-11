package dev.newdimension;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Starter entrypoint for the New Dimension mod.
 *
 * IMPORTANT: This is not yet a complete playable implementation. The command below
 * deliberately reports that the dimension backend still needs to be implemented,
 * rather than pretending a dimension was successfully created.
 */
public final class NewDimensionMod implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("newdimension")
                .then(Commands.literal("help")
                    .executes(context -> {
                        context.getSource().sendSuccess(
                            () -> Component.literal(
                                "Usage: /newdimension create <id> <surface_block> <portal_frame_block> "
                                + "<survival|creative|adventure|spectator> <shared|separate>"
                            ), false);
                        context.getSource().sendFailure(Component.literal(
                            "This starter build does not yet create playable dimensions. "
                            + "See README.md for the missing implementation."));
                        return 0;
                    }))
            );
        });
    }
}
