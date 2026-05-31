# Handoff: Antique Atlas 4 port to MC 26.1.2

## Repo location
`d:\progects\AntiqueAtlas4\antique-atlas`  
Branch: `port-26.1.2`

## Build command
```
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
./gradlew build
```

## Current state
**7 compiler errors remain** (everything else compiles). Fix them and the build will succeed.

---

## Error 1: `AtlasScreen.java:809` — `render` method not found on MarkerModal

```java
// File: src/main/java/folk/sisby/antique_atlas/gui/AtlasScreen.java
// Line ~809:
markerModal.render(context, trueMouseX, trueMouseY, partialTick);
markerModal.render(context, mouseX, mouseY, partialTick);  // (also nearby lines)
```
**Fix:** Replace `markerModal.render(` → `markerModal.extractRenderState(` (MC 26.1 renamed Renderable.render to extractRenderState)

---

## Error 2: `AtlasScreen.java:819` — `setTooltipForNextFrame` type inference

```java
// Line ~819 in AtlasScreen.java:
context.setTooltipForNextFrame(font, Stream.concat(...).map(FormattedText::of).map(FormattedCharSequence::forward).toList(), 0, 0);
```
**Fix:** `FormattedCharSequence::forward` doesn't work here. Replace the entire line with:
```java
List<net.minecraft.network.chat.Component> tooltipLines = Stream.concat(
    Stream.of(name),
    hoveredLandmark.getOrDefault(LandmarkComponentTypes.LORE, new ArrayList<>()).stream().map(t -> t.copy().withStyle(ChatFormatting.GRAY))
).toList();
context.setTooltipForNextFrame(font, tooltipLines.get(0), 0, 0);
// Note: MC 26.1 setTooltipForNextFrame only shows one Component — simplification accepted
```

---

## Error 3: `AtlasScreen.java:822` — `getGameProfile().getName()` missing

```java
// Line ~822:
hoveredFriend.username().equals(Minecraft.getInstance().player.getGameProfile().getName())
```
**Fix:** `GameProfile.getName()` was removed. Use:
```java
hoveredFriend.username().equals(Minecraft.getInstance().player.getGameProfile().name())
```

---

## Error 4: `MarkerModal.java:188` — `textField.mouseClicked(double, double, int)`

```java
// File: src/main/java/folk/sisby/antique_atlas/gui/MarkerModal.java
// Line ~188:
return super.mouseClicked(event, consume) || textField.mouseClicked(event.x(), event.y(), event.button());
```
`textField` is an `EditBox`. In MC 26.1, `EditBox.mouseClicked` takes `(MouseButtonEvent, boolean)`.  
**Fix:**
```java
return super.mouseClicked(event, consume) || textField.mouseClicked(event, consume);
```

---

## Error 5: `ScrollBoxComponent.java:44` — `super.render(...)` not found

```java
// File: src/main/java/folk/sisby/antique_atlas/gui/core/ScrollBoxComponent.java
// Line ~44:
super.render(context, mouseX, mouseY, partialTick);
```
**Fix:** `render` → `extractRenderState`

---

## Error 6: `TexturePreviewButton.java:53` — `super.render(...)` not found

Same as Error 5.  
**Fix:** `super.render(` → `super.extractRenderState(`

---

## Error 7: `StructureTileProviders.java:159` — `tag.identifier()` not found

```java
// Line ~159:
StructureTileProvider provider = tagTiles.get(tag.identifier());
```
`tag` is `TagKey<Structure>`. `TagKey.identifier()` doesn't exist — use `tag.location()`.  
**Fix:**
```java
StructureTileProvider provider = tagTiles.get(tag.location());
```

---

## After fixing all 7 errors

Run `./gradlew build`. Expected result: `BUILD SUCCESSFUL`, JAR in `build/libs/`.

Then create `PORTING_NOTES.md` documenting:
- Tile/marker rendering disabled in GUI (Matrix3x2fStack vs PoseStack incompatibility — MC 26.1 GUI is 2D-only)
- Handheld atlas rendering disabled (SubmitNodeCollector API needs full reimplementation)
- Atlas book model override disabled (ItemModelShaper removed — needs data-driven items/book.json)
- ItemProperties.register removed (data-driven item models)
- ConventionalBiomeTags v1 removed — now v2 only
- MetadataSectionType is now a record, not an interface
- SimpleJsonResourceReloadListener now takes Codec, not Gson
- KeyMapping.Category now requires Identifier

## Key architecture changes in MC 26.1 (for reference)
- GUI rendering: `render(GuiGraphics, ...)` → `extractRenderState(GuiGraphicsExtractor, ...)`
- GUI matrices: `PoseStack` → `Matrix3x2fStack` (2D only, JOML push/pop: `pushMatrix()`/`popMatrix()`)
- `blit` / `blitSprite` now need `RenderPipelines.GUI_TEXTURED` as first arg
- `drawString` → `text` on `GuiGraphicsExtractor`
- `renderTooltip` → `setTooltipForNextFrame`
- Event handlers: `mouseClicked(double,double,int)` → `mouseClicked(MouseButtonEvent, boolean)`
- `keyPressed(int,int,int)` → `keyPressed(KeyEvent)`
- `ResourceKey.location()` → `ResourceKey.identifier()`
- `TagKey.location()` still exists (keep as-is)
- `InteractionResultHolder<T>` removed → `InteractionResult` directly
- `ChunkPos(BlockPos)` constructor removed → `ChunkPos.containing(BlockPos)`
- `RenderSystem.setShader/setShaderTexture/BufferUploader.drawWithShader` removed → `RenderType.draw(MeshData)`
