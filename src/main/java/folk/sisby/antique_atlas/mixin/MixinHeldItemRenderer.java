package folk.sisby.antique_atlas.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.gui.HandheldAtlasRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class MixinHeldItemRenderer {
	@Inject(method = "renderMap", at = @At("HEAD"), cancellable = true)
	protected void renderFirstPersonAtlas(PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, ItemStack stack, CallbackInfo ci) {
		if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return;
		if (!(AntiqueAtlas.isHandheldAtlas(stack))) return;
		HandheldAtlasRenderer.fromContext(Minecraft.getInstance().player).renderHandheldAtlas(matrices, submitNodeCollector, light);
		ci.cancel();
	}
	// NOTE: enableFirstPersonAtlasRendering (@ModifyExpressionValue on renderArmWithItem)
	// removed — renderArmWithItem in MC 26.1.2 no longer calls ItemStack.is(Item).
}
