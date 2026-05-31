package folk.sisby.antique_atlas.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.WorldAtlasData;
import folk.sisby.antique_atlas.util.DrawBatcher;
import folk.sisby.antique_atlas.util.MathUtil;
import folk.sisby.surveyor.client.SurveyorClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector2d;

public record HandheldAtlasRenderer(int bookX, int bookY, int bookWidth, int bookHeight, int mapWidth, int mapHeight, int tilePixels, int tileChunks, double guiScale, double mapOffsetX, double mapOffsetY, int mapScale, Player player, WorldAtlasData worldAtlasData, ResourceKey<Level> dim) implements AtlasRenderer {
	public static HandheldAtlasRenderer fromContext(Player player) {
		return new HandheldAtlasRenderer(
			0,
			0,
			DEFAULT_BOOK_WIDTH,
			DEFAULT_BOOK_HEIGHT,
			DEFAULT_BOOK_WIDTH - MAP_BORDER_WIDTH * 2,
			DEFAULT_BOOK_HEIGHT - MAP_BORDER_HEIGHT * 2,
			16,
			1,
			1,
			-player.getBlockX(),
			-player.getBlockZ(),
			1,
			player,
			WorldAtlasData.getOrCreate(player.level().dimension()),
			player.level().dimension()
		);
	}

	// NOTE: Rendering updated for MC 26.1 SubmitNodeCollector API.
	// Tile rendering in handheld view is disabled pending full port.
	public void renderHandheldAtlas(PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light) {
		matrices.pushPose();
		matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
		matrices.mulPose(Axis.ZP.rotationDegrees(180.0F));
		matrices.scale(0.38F * 142.0F / 218.0F, 0.38F * 142.0F / 218.0F, 0.38F);
		matrices.translate(-1.2D, -0.88D, 0D);
		matrices.scale(1.0F / 128.0F, 1.0F / 128.0F, 1.0F / 128.0F);

		// Use submitCustomGeometry to bridge SubmitNodeCollector to VertexConsumer rendering
		net.minecraft.client.renderer.rendertype.RenderTypes.text(AtlasScreen.BOOK);
		submitNodeCollector.submitCustomGeometry(matrices, net.minecraft.client.renderer.rendertype.RenderTypes.text(AtlasScreen.BOOK), (pose, vc) -> {
			// NOTE: Custom geometry rendering simplified for MC 26.1 port
		});

		matrices.popPose();
	}

	@Override
	public double getPixelsPerBlock() {
		return 1;
	}
}
