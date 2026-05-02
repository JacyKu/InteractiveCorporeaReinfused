package shblock.interactivecorporea.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2CharFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.util.math.vector.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.util.MathUtil;
import shblock.interactivecorporea.common.util.Perlin;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.core.helper.Vector3;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {
  private static final RenderType STAR = ModRenderTypes.star;
  private static final float TEXT_SHADOW_Z_OFFSET = -.25F;
  private static final float TEXT_FOREGROUND_Z_OFFSET = -.5F;

  private static final Minecraft mc = Minecraft.getInstance();

  /**
   * Renders the halo arc using a custom shader RenderType (POSITION_COLOR_TEX).
    * UV.x = world azimuth / (2 * PI), UV.y = 0 (bottom) or 1 (top).
    * This makes the shader sample stay pinned to world directions like a skybox.
   */
  public static void renderPartialHaloShader(MatrixStack ms, RenderType type,
      double radius, double width, double height, double fadeWidth,
      double worldRotRad,
      float r, float g, float b, float alpha) {
    double fullWidth = width + fadeWidth;
    if (fullWidth <= 0D) return;

    MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
    VertexConsumer buffer = buffers.getBuffer(type);
    Matrix4f matrix = ms.getLast().getMatrix();

    for (double angle = -fullWidth; angle < fullWidth; angle += Math.PI / 360D) {
      float xp = (float) (Math.sin(angle) * radius);
      float zp = (float) (Math.cos(angle) * radius);

      double minDistToEdge = Math.min(Math.abs(fullWidth - angle), Math.abs(-fullWidth - angle));
      float a = alpha;
      if (minDistToEdge < fadeWidth) {
        a *= (float) Math.sin((minDistToEdge / fadeWidth) * (Math.PI / 2));
      }

      // World-space U: world_azimuth = angle - worldRotRad (from R_y(-worldRotRad) matrix).
      // Dividing by 2 * PI normalises to 0..1 over the full circle. Since u = world_azimuth / (2 * PI),
      // the worldRotRad cancels for any fixed world direction, giving a static skybox UV.
      float u = (float) ((angle - worldRotRad) / (2.0 * Math.PI));
      buffer.vertex(matrix, xp, (float) (-height), zp).color(r, g, b, a).uv(u, 0f).endVertex();
      buffer.vertex(matrix, xp, (float) ( height), zp).color(r, g, b, a).uv(u, 1f).endVertex();
    }
    buffers.endBatch(type);
  }

  public static void renderPartialHalo(MatrixStack ms, double radius, double width, double height, double fadeWidth, float r, float g, float b, float alpha) {
    MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
    VertexConsumer buffer = buffers.getBuffer(ModRenderTypes.halo);

    Matrix4f matrix = ms.getLast().getMatrix();
    double fullWidth = width + fadeWidth;
    for (double angle = -fullWidth; angle < fullWidth; angle += Math.PI / 360F) {
      float xp = (float) (Math.sin(angle) * radius);
      float zp = (float) (Math.cos(angle) * radius);

      double minDistToEdge = Math.min(
          Math.abs(fullWidth - angle),
          Math.abs(-fullWidth - angle)
      );
      float currentAlpha = alpha;
      if (minDistToEdge < fadeWidth) {
        currentAlpha *= Math.sin((minDistToEdge / fadeWidth) * (Math.PI / 2));
      }

      buffer.vertex(matrix, xp, (float) (-height), zp).color(r, g, b, currentAlpha).endVertex();
      buffer.vertex(matrix, xp, (float) (height), zp).color(r, g, b, currentAlpha).endVertex();
    }
    buffers.endBatch();
  }

  public static void renderWavyPartialHalo(MatrixStack ms, double radius, double width, double height, double fadeWidth, double waveHeight, double waveFrequency, double waveSpeed, double waveOffset, float r, float g, float b, float alpha) {
    double fullWidth = width + fadeWidth;
    if (fullWidth <= 0D) return;

    MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
    VertexConsumer buffer = buffers.getBuffer(ModRenderTypes.halo);

    Matrix4f matrix = ms.getLast().getMatrix();
    for (double angle = -fullWidth; angle < fullWidth; angle += Math.PI / 360F) {
      double progress = (angle + fullWidth) / (fullWidth * 2D);
      double topWave = wave(progress, waveFrequency, waveSpeed, waveOffset);
      double bottomWave = wave(progress, waveFrequency * .8D + .7D, -waveSpeed * .85D, waveOffset + Math.PI * .8D);
      float xp = (float) (Math.sin(angle) * radius);
      float zp = (float) (Math.cos(angle) * radius);

      double minDistToEdge = Math.min(
          Math.abs(fullWidth - angle),
          Math.abs(-fullWidth - angle)
      );
      float currentAlpha = alpha;
      if (minDistToEdge < fadeWidth) {
        currentAlpha *= Math.sin((minDistToEdge / fadeWidth) * (Math.PI / 2));
      }

      float bottomHeight = (float) Math.max(.02D, height + bottomWave * waveHeight);
      float topHeight = (float) Math.max(.02D, height + topWave * waveHeight);
      buffer.vertex(matrix, xp, -bottomHeight, zp).color(r, g, b, currentAlpha).endVertex();
      buffer.vertex(matrix, xp, topHeight, zp).color(r, g, b, currentAlpha).endVertex();
    }
    buffers.endBatch();
  }

  private static double wave(double progress, double frequency, double speed, double offset) {
    double time = RenderTick.total * speed + offset;
    return Math.sin(progress * Math.PI * 2D * frequency + time) * .65D
        + Math.sin(progress * Math.PI * 2D * (frequency * .5D + 1D) - time * .7D) * .35D;
  }

  private static double calcFullTextOnHaloRadians(Font font, String text, float textScale, double radius) {
    double result = 0;
    for (char c : text.toCharArray()) {
      float width = font.width(String.valueOf(c)) * textScale;
      result += MathUtil.calcRadiansFromChord(radius, width);
    }
    return result;
  }

  public static double calcTextOnHaloRadians(Font font, String text, float textScale, double radius) {
    return calcFullTextOnHaloRadians(font, text, textScale, radius);
  }

  public static double renderTextOnHaloCentered(MatrixStack ms, Font font, String text, double radius, float textScale, int color, Int2IntFunction bgColorProvider, Int2CharFunction additionalCharProvider) {
    return renderTextOnHaloCentered(ms, font, text, radius, textScale, i -> color, bgColorProvider, additionalCharProvider);
  }

  public static double renderTextOnHaloCentered(MatrixStack ms, Font font, String text, double radius, float textScale, Int2IntFunction colorProvider, Int2IntFunction bgColorProvider, Int2CharFunction additionalCharProvider) {
    double yOffset = -font.lineHeight * textScale / 2D;
    ms.push();
    double fullRot = calcFullTextOnHaloRadians(font, text, textScale, radius);
    ms.rotate(new Quaternion(Vector3f.XP, 180, true));
    ms.rotate(Vector3f.YN.rotation((float) (fullRot / 2 + Math.PI)));
    double rot = 0;
    MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
    char[] chrArray = text.toCharArray();
    for (int i = 0; i < chrArray.length + 1; i++) {
      ms.push();
      ms.rotate(Vector3f.YP.rotation((float) rot));
      String chr = "";
      if (i != chrArray.length) {
        chr = String.valueOf(chrArray[i]);
      }
      float width = font.width(chr) * textScale;
      ms.translate(0, yOffset, MathUtil.calcChordCenterDistance(radius, width));
      ms.scale(textScale, textScale, textScale);
      int color = colorProvider.applyAsInt(i);
      int shadeColor = (color & 16579836) >> 2 | color & -16777216;
      if (!chr.isEmpty()) {
        ms.push();
        ms.translate(0, 0, TEXT_SHADOW_Z_OFFSET);
        Matrix4f glyphShadowMatrix = ms.getLast().getMatrix();
        font.drawInBatch(chr, 1, 1, shadeColor, false, glyphShadowMatrix, buffers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        ms.pop();
        ms.push();
        ms.translate(0, 0, TEXT_FOREGROUND_Z_OFFSET);
        Matrix4f glyphMatrix = ms.getLast().getMatrix();
        font.drawInBatch(chr, 0, 0, color, false, glyphMatrix, buffers, Font.DisplayMode.NORMAL, bgColorProvider.applyAsInt(i), 0xF000F0);
        ms.pop();
      }
      char additionalChr = additionalCharProvider.apply(i);
      if (additionalChr != 0) {
        String additionalChrStr = String.valueOf(additionalChr);
        ms.push();
        ms.translate(0, 0, TEXT_SHADOW_Z_OFFSET);
        Matrix4f additionalShadowMatrix = ms.getLast().getMatrix();
        font.drawInBatch(additionalChrStr, 1, 1, shadeColor, false, additionalShadowMatrix, buffers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        ms.pop();
        ms.push();
        ms.translate(0, 0, TEXT_FOREGROUND_Z_OFFSET);
        Matrix4f additionalMatrix = ms.getLast().getMatrix();
        font.drawInBatch(additionalChrStr, 0, 0, color, false, additionalMatrix, buffers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        ms.pop();
      }
      ms.pop();

      rot += MathUtil.calcRadiansFromChord(radius, width);
    }
    ms.pop();
    buffers.endBatch();

    return fullRot;
  }

  public static double renderTextOnHaloCentered(MatrixStack ms, Font font, String text, double radius, float textScale, int color) {
    return renderTextOnHaloCentered(ms, font, text, radius, textScale, color, i -> 0, i -> (char) 0);
  }

  public static Vector3 worldPosToLocalPos(Vector3 worldPos) {
    Camera info = mc.gameRenderer.getMainCamera();
    return new Vector3(info.getPosition())
        .subtract(worldPos);
  }

  public static Vec2d calcNDC(Vector3 worldCoord) {
    Camera info = mc.gameRenderer.getMainCamera();
    Vector3 pos = worldPosToLocalPos(worldCoord);
    Vector3d pos3d = new Vector3d(pos.x, pos.y, pos.z)
        .rotateYaw((float) Math.toRadians(info.getYRot() + 180))
        .rotatePitch((float) Math.toRadians(-info.getXRot()));
    Matrix4f matrix = new Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix());
    Vector4f vec4 = new Vector4f((float) pos3d.x, (float) pos3d.y, (float) pos3d.z, 1F);
    vec4.transform(matrix);
    vec4.perspectiveDivide();
    return new Vec2d(vec4.getX(), vec4.getY());
  }

  public static Vec2d texCoordFromNDC(Vec2d ndc) {
    return ndc.copy().add(1, 1).mul(.5);
  }

  private static final Perlin starPerlin = new Perlin();

  // [Botania Copy] RenderHelper.renderStar() without changing size and with random animation based on perlin noise
  public static void renderPerlinStar(PoseStack ms, MultiBufferSource buffers, int color, float xScale, float yScale, float zScale, double seed) {
    VertexConsumer buffer = buffers.getBuffer(STAR);

    float f2 = .15F;

    ms.pushPose();
    ms.scale(xScale, yScale, zScale);

    double noisePos = RenderTick.total * .005;

    for (int i = 0; i < 256; i++) {
      ms.pushPose();
      double z = i * 12.3456789 + seed;
      ms.mulPose(new Quaternion(
          (float) (starPerlin.perlin(noisePos, 0, z) * Math.PI * 2),
          (float) (starPerlin.perlin(noisePos, 10, z) * Math.PI * 2),
          (float) (starPerlin.perlin(noisePos, 20, z) * Math.PI * 2),
          false));
//      if (starPerlin.perlin(noisePos, 0, perlinZ) * Math.PI == starPerlin.perlin(noisePos, 100, perlinZ) * Math.PI)
//        System.out.println(starPerlin.perlin(noisePos, 100, perlinZ) * Math.PI);
      float f3 = (float) (starPerlin.perlin(noisePos, 30, z) * 20F + 5F + f2 * 10F);
      float f4 = (float) (starPerlin.perlin(noisePos, 40, z) * 2F + 1F + f2 * 2F);
      float r = ((color & 0xFF0000) >> 16) / 255F;
      float g = ((color & 0xFF00) >> 8) / 255F;
      float b = (color & 0xFF) / 255F;
        org.joml.Matrix4f poseMatrix = ms.last().pose();
        Runnable center = () -> buffer.vertex(poseMatrix, 0, 0, 0).color(r, g, b, 1F).endVertex();
      Runnable[] vertices = {
          () -> buffer.vertex(poseMatrix, -0.866F * f4, f3, -0.5F * f4).color(0, 0, 0, 0).endVertex(),
          () -> buffer.vertex(poseMatrix, 0.866F * f4, f3, -0.5F * f4).color(0, 0, 0, 0).endVertex(),
          () -> buffer.vertex(poseMatrix, 0, f3, f4).color(0, 0, 0, 0).endVertex(),
          () -> buffer.vertex(poseMatrix, -0.866F * f4, f3, -0.5F * f4).color(0, 0, 0, 0).endVertex()
      };
      RenderHelper.triangleFan(center, vertices);
      ms.popPose();
    }

    ms.popPose();
  }

  public static void renderFlatItem(MatrixStack ms, ItemStack stack) {
    ms.push();
    ms.scale(1F, 1F, .001F);

    BakedModel model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
    if (model.usesBlockLight()) {
      Lighting.setupFor3DItems();
    } else {
      Lighting.setupForFlatItems();
    }
    ms.last().normal().identity();

    MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
    mc.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, ms, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY, model);

    buffers.endBatch();

    ms.pop();
    Lighting.setupFor3DItems();
  }

  public static BlockEntity createRendererDummy(Block block) {
    if (block instanceof EntityBlock entityBlock) {
      BlockEntity blockEntity = entityBlock.newBlockEntity(net.minecraft.core.BlockPos.ZERO, block.defaultBlockState());
      if (blockEntity != null && mc.level != null) {
        blockEntity.setLevel(mc.level);
      }
      return blockEntity;
    }
    return null;
  }

  public static void renderBlockEntity(PoseStack ms, MultiBufferSource buffers, int combinedLight, int combinedOverlay, BlockEntity blockEntity, BlockEntity fallback) {
    BlockEntity target = blockEntity != null ? blockEntity : fallback;
    if (target == null) {
      return;
    }
    if (target.getLevel() == null && mc.level != null) {
      target.setLevel(mc.level);
    }
    mc.getBlockEntityRenderDispatcher().renderItem(target, ms, buffers, combinedLight, combinedOverlay);
  }

  /**
   * Apply the stippling effect ("fake" alpha) to your render
   * @param alpha the "fake" alpha level (0~16, 0 is fully transparent, 16 is fully opaque)
   * @param renderer
   */
  public static void applyStippling(int alpha, Runnable renderer) {
    renderer.run();
  }

  public static void applyStippling(double alpha, Runnable renderer) {
    applyStippling((int) Math.round(alpha * 16), renderer);
  }
}
