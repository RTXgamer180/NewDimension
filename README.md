# New Dimension — command implementation (experimental)

This project implements a persistent command/configuration layer for custom dimension definitions:
- `/newdimension create <id> <surface_block> <portal_frame_block> <gamemode> <shared|separate>`
- `/newdimension list`
- `/newdimension info <id>`
- `/newdimension remove <id>`

Definitions are stored in `new-dimensions.json` in the world save folder.

## Important limitation
This does NOT yet create playable dimensions, generate chunks, activate portals, move players between dimensions, or swap inventories. The dynamic dimension library found in research (DimLib) only lists Minecraft 1.21.1 and 1.20.4 support, not 26.2, so wiring it in would not be honest or safe. The commands here manage persistent definitions and clearly report that the runtime backend is missing.

## Build online
Upload all files to a GitHub repository, preserving `.github/workflows/build.yml`, then use Actions -> Build New Dimension mod. Dependency versions must match the actual Fabric 26.2 release metadata.
