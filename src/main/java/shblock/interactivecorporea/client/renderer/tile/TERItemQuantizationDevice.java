package shblock.interactivecorporea.client.renderer.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.Mth;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.tile.TileItemQuantizationDevice;
import vazkii.botania.common.block.BotaniaBlocks;

public class TERItemQuantizationDevice implements BlockEntityRenderer<TileItemQuantizationDevice> {
  private static final BlockEntity CORPOREA_INDEX_RENDER_BE = RenderUtil.createRendererDummy(BotaniaBlocks.corporeaIndex);
  private static final BlockEntity LIGHT_RELAY_RENDER_BE = RenderUtil.createRendererDummy(BotaniaBlocks.lightRelayDefault);

  public TERItemQuantizationDevice(BlockEntityRendererProvider.Context context) {
  }

  @Override
  public void render(TileItemQuantizationDevice tile, float pt, PoseStack ms, MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
    ms.pushPose();
    float scale = (float) (tile.getLightRelayRenderScale() + .2);
    ms.translate(.5, .5, .5);
    ms.scale(scale, scale, scale);
    int color = Mth.hsvToRgb((float) ((RenderTick.total / 200F) % 1F), 1F, 1F) | 0x80 << 24;
    RenderUtil.renderPerlinStar(ms, buffers, color, .06F, .06F, .06F, tile.getBlockPos().asLong());
    ms.popPose();

    ms.pushPose();
    ms.translate(.5, .5, .5);
    ms.scale(scale, scale, scale);
    ms.translate(-.5, -.5, -.5);
    RenderUtil.renderBlockEntity(ms, buffers, combinedLight, combinedOverlay, LIGHT_RELAY_RENDER_BE, LIGHT_RELAY_RENDER_BE);
    ms.popPose();

    ms.pushPose();
    ms.translate(.25, Math.sin(RenderTick.total * .1) * .1 + .8, .25);
    ms.scale(.5F, .5F, .5F);
    RenderUtil.renderBlockEntity(ms, buffers, combinedLight, combinedOverlay, CORPOREA_INDEX_RENDER_BE, null);
    ms.popPose();
  }
}
