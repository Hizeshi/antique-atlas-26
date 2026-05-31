# Porting Notes: Antique Atlas 4 to Minecraft 26.1.2

This branch ports Antique Atlas 4 to Minecraft 26.1.2 / Fabric 0.19.2 far enough to compile successfully.

## Current build status

`./gradlew build` completes successfully with JDK 25.

## Compatibility notes

- Tile and marker rendering in the GUI is currently disabled because Minecraft 26.1 GUI rendering moved from `PoseStack` to `Matrix3x2fStack`. The new GUI stack is 2D-only, so the old 3D-style rendering path needs a dedicated rewrite.
- Handheld atlas rendering is currently disabled. The new `SubmitNodeCollector` rendering API requires a full reimplementation of the old handheld item renderer.
- Atlas book model override is disabled. `ItemModelShaper` was removed and this needs to be migrated to data-driven item model definitions, including an `items/book.json` setup.
- `ItemProperties.register` was removed. Dynamic item model behavior now needs to be represented through data-driven item models.
- `ConventionalBiomeTags` v1 was removed. The port now targets the v2 conventional biome tags API only.
- `MetadataSectionType` is now a record instead of an interface, so metadata registration code was adjusted accordingly.
- `SimpleJsonResourceReloadListener` now takes a `Codec` instead of a `Gson` instance.
- `KeyMapping.Category` now requires an `Identifier`.

## Final compile fixes

- Replaced remaining GUI `render(...)` calls with `extractRenderState(...)` where Minecraft 26.1 renamed the render extraction entry point.
- Updated marker modal mouse click forwarding to use the new `MouseButtonEvent`-based `EditBox.mouseClicked(...)` signature.
- Simplified landmark tooltip creation to pass a single `Component` to `setTooltipForNextFrame(...)`, matching the Minecraft 26.1 API.
- Updated game profile name access from `getName()` to `name()`.
- Updated structure tag lookup from `TagKey.identifier()` to `TagKey.location()`.
