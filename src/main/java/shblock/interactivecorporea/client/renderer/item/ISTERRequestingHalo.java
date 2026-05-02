package shblock.interactivecorporea.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import shblock.interactivecorporea.ModConfig;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.util.RenderTick;
import vazkii.botania.client.render.block_entity.CorporeaCrystalCubeBlockEntityRenderer;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.helper.VecHelper;

public class ISTERRequestingHalo extends BlockEntityWithoutLevelRenderer {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final BlockEntity CORPOREA_INDEX_RENDER_BE = RenderUtil.createRendererDummy(BotaniaBlocks.corporeaIndex);

  public ISTERRequestingHalo() {
    super(null, null);
  }

  @Override
  public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
    ms.pushPose();
    switch (transformType) {
      case FIRST_PERSON_LEFT_HAND:
      case THIRD_PERSON_LEFT_HAND:
      case FIRST_PERSON_RIGHT_HAND:
      case THIRD_PERSON_RIGHT_HAND:
        float s = .75F;
        ms.scale(s, s, s);
        ms.translate(.15, 0, 0);
        break;
      case GROUND:
        float sg = 1F;
        ms.scale(sg, sg, sg);
        break;
      default:
        break;
    }

    ms.pushPose();
    ms.scale(.5F, .5F, .5F);
    ms.translate(.5, .5, .5);
    RenderUtil.renderBlockEntity(ms, buffers, combinedLight, combinedOverlay, CORPOREA_INDEX_RENDER_BE, null);
    ms.popPose();

    if (ModConfig.CLIENT.itemRequestingHaloAnimation.get()) {
      ms.pushPose();
      ms.translate(.5, .5, .5);
      int color = Mth.hsvToRgb((float) ((RenderTick.total / 200F) % 1F), 1F, 1F) | 150 << 24;
      RenderUtil.renderPerlinStar(ms, buffers, color, .04F, .04F, .04F, 0);
      ms.popPose();
    }

    BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
    VertexConsumer buffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());

    ms.pushPose();
    ms.scale(.5F, .5F, .5F);
    ms.translate(.5, .5, .5);
    ms.translate(.5, .5, .5);
    ms.translate(-.5, -.5, -.5);
    double per = 0.02;
    for (double i = 0; i < 1; i += per) {
      ms.pushPose();
      ms.translate(.5, .5, .5);
      ms.mulPose(VecHelper.rotateZ((float) (360F * i)));
      ms.translate(-.5, -.5, -.5);
      ms.translate(0, .8, 0);

      ms.translate(.5, .5, .5);
      double r = RenderTick.total * .1;
      if (!ModConfig.CLIENT.itemRequestingHaloAnimation.get()) {
        r = .2;
      }
      ms.mulPose(new Quaternionf().rotationXYZ(
          (float) ((i + Math.sin(r) / 5) * Math.PI * 4),
          (float) r,
          (float) (Math.PI * Math.sin(r) * .25)
      ));
      ms.translate(-.5, -.5, -.5);

      if (CorporeaCrystalCubeBlockEntityRenderer.cubeModel != null) {
        blockRenderer.getModelRenderer().renderModel(ms.last(), buffer, null, CorporeaCrystalCubeBlockEntityRenderer.cubeModel, 1F, 1F, 1F, combinedLight, combinedOverlay);
      }
      ms.popPose();
    }
    ms.popPose();

    ms.popPose();
  }
}
