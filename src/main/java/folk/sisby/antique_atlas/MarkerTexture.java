package folk.sisby.antique_atlas;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Vector2d;

public record MarkerTexture(Identifier id, Identifier accentId, Identifier item, int offsetX, int offsetY, int textureWidth, int textureHeight, int mipLevels, int nearClip, int farClip) {
	public static Identifier idToTexture(Identifier id) {
		return id.withPrefix("textures/atlas/marker/").withSuffix(".png");
	}

	public static MarkerTexture ofId(Identifier id, Identifier item, int offsetX, int offsetY, int width, int height, int mipLevels, int nearClip, int farClip, boolean accent) {
		return new MarkerTexture(idToTexture(id), accent ? idToTexture(id.withSuffix("_accent")) : null, item, offsetX, offsetY, width, height, mipLevels, nearClip, farClip);
	}

	public static MarkerTexture centered(Identifier id, Identifier item, int width, int height, int mipLevels, int nearClip, int farClip, boolean accent) {
		return ofId(id, item, -width / 2, -height / 2, width, height, mipLevels, nearClip, farClip, accent);
	}

	public static final MarkerTexture DEFAULT = centered(AntiqueAtlas.id("custom/point"), Identifier.fromNamespaceAndPath("minecraft", "emerald"), 32, 32, 0, 1, Integer.MAX_VALUE, true);

	public Identifier keyId() {
		return Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath().substring("textures/atlas/marker/".length(), id.getPath().length() - 4));
	}

	public String displayId() {
		return id.getNamespace().equals(AntiqueAtlas.ID) ? keyId().getPath() : keyId().toString();
	}

	public int fullTextureWidth() {
		int width = textureWidth;
		for (int i = 0; i < mipLevels; i++) {
			width += textureWidth >> (i + 1);
		}
		return width;
	}

	public int getU(int mipLevel) {
		int currentMipLevel = mipLevel - 1;
		int u = 0;
		while (currentMipLevel >= 0) {
			u += textureWidth / (1 << currentMipLevel);
			currentMipLevel--;
		}
		return u;
	}

	public Vector2d getCenter(int tileChunks) {
		int mipLevel = Mth.clamp(Mth.ceillog2(tileChunks), 0, mipLevels);
		return new Vector2d(((double) offsetX + (double) textureWidth / 2.0) / (double) (1 << mipLevel), ((double) offsetY + (double) textureHeight / 2.0) / (double) (1 << mipLevel));
	}

	public double getSquaredSize(int tileChunks) {
		int mipLevel = Mth.clamp(Mth.ceillog2(tileChunks), 0, mipLevels);
		return textureWidth * textureHeight / (double) (1 << mipLevel);
	}

	public void drawIcon(GuiGraphicsExtractor context, int x, int y, float[] accent) {
		var pipeline = net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;
		context.blit(pipeline, id, x, y, 0f, 0f, textureWidth, textureHeight, textureWidth, textureHeight, fullTextureWidth(), textureHeight);
		if (accentId != null && accent != null) {
			int accentArgb = net.minecraft.util.ARGB.color(255, (int) (accent[0] * 255), (int) (accent[1] * 255), (int) (accent[2] * 255));
			context.blit(pipeline, accentId, x, y, 0f, 0f, textureWidth, textureHeight, textureWidth, textureHeight, fullTextureWidth(), textureHeight, accentArgb);
		}
	}

	public void draw(GuiGraphicsExtractor context, double markerX, double markerY, float markerScale, int tileChunks, float[] accent, float tint, float alpha) {
		if (alpha == 0) return;
		context.pose().pushMatrix();
		context.pose().translate((float) markerX, (float) markerY);
		context.pose().scale(markerScale, markerScale);
		int mainArgb = ARGB.color((int) (alpha * 255), (int) (tint * 255), (int) (tint * 255), (int) (tint * 255));
		int accentArgb = accent != null ? ARGB.color((int) (alpha * 255), (int) (tint * accent[0] * 255), (int) (tint * accent[1] * 255), (int) (tint * accent[2] * 255)) : 0;
		var pipeline = net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;
		if (tileChunks > 1 && mipLevels > 0) {
			int mipLevel = Mth.clamp(Mth.ceillog2(tileChunks), 0, mipLevels);
			int w = textureWidth / (1 << mipLevel);
			int h = textureHeight / (1 << mipLevel);
			int ox = offsetX / (1 << mipLevel);
			int oy = offsetY / (1 << mipLevel);
			context.blit(pipeline, id, ox, oy, (float) getU(mipLevel), 0f, w, h, w, h, fullTextureWidth(), textureHeight, mainArgb);
			if (accentId != null && accent != null) {
				context.blit(pipeline, accentId, ox, oy, (float) getU(mipLevel), 0f, w, h, w, h, fullTextureWidth(), textureHeight, accentArgb);
			}
		} else {
			context.blit(pipeline, id, offsetX, offsetY, 0f, 0f, textureWidth, textureHeight, textureWidth, textureHeight, fullTextureWidth(), textureHeight, mainArgb);
			if (accentId != null && accent != null) {
				context.blit(pipeline, accentId, offsetX, offsetY, 0f, 0f, textureWidth, textureHeight, textureWidth, textureHeight, fullTextureWidth(), textureHeight, accentArgb);
			}
		}
		context.pose().popMatrix();
	}
}
