package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import shblock.interactivecorporea.client.render.ModRenderTypes;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.item.HaloInterfaceStyle;

public final class HaloInterfaceBackground {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final double FADE_WIDTH = .3D;
  private static final double MAX_WIDTH = Math.PI * .25D;

  private HaloInterfaceBackground() {
  }

  public static void render(MatrixStack ms, double radius, double height, double progress,
      HaloInterfaceStyle style, float[] haloTint, double worldRotDeg) {
    switch (style) {
      case CLASSIC:
        renderClassic(ms, radius, height, progress);
        break;
      case MANA:
        renderMana(ms, radius, height, progress);
        break;
      case CORPOREA:
        renderCorporea(ms, radius, height, progress);
        break;
      case CLOUDS:
        renderClouds(ms, radius, height, progress, ModRenderTypes.haloCloud, haloTint,
            Math.toRadians(worldRotDeg));
        break;
      case SPACE:
        renderSpace(ms, radius, height, progress, ModRenderTypes.haloSpace, haloTint,
            Math.toRadians(worldRotDeg));
        break;
      case FALLINGSTARS:
        renderFallingStars(ms, radius, height, progress, ModRenderTypes.haloFallingStars, haloTint,
            Math.toRadians(worldRotDeg));
        break;
      case LAVALAMP:
        renderLavaLamp(ms, radius, height, progress, ModRenderTypes.haloLavaLamp, haloTint,
            Math.toRadians(worldRotDeg));
        break;
      case DEPTHMAP:
        renderDepthMap(ms, radius, height, progress, ModRenderTypes.haloDepthMap, haloTint,
            Math.toRadians(worldRotDeg));
        break;
      case FOGGYCLOUDS:
        renderFoggyCloud(ms, radius, height, progress, ModRenderTypes.haloFoggyCloud, haloTint,
            Math.toRadians(worldRotDeg));
        break;
      case GLASSLIQUID:
        renderGlassLiquid(ms, radius, height, progress, ModRenderTypes.haloGlassLiquid, haloTint,
            Math.toRadians(worldRotDeg));
        break;
      case METALCLOUDS:
        renderMetalCloud(ms, radius, height, progress, ModRenderTypes.haloMetalCloud, haloTint,
            Math.toRadians(worldRotDeg));
        break;
      case SMOKISH:
        renderSmokish(ms, radius, height, progress, ModRenderTypes.haloSmokish, haloTint,
            Math.toRadians(worldRotDeg));
        break;
      case SPLIT:
        renderSplit(ms, radius, height, progress, ModRenderTypes.haloSplit, haloTint,
            Math.toRadians(worldRotDeg));
        break;
      case WAVYFOG:
        renderWavyFog(ms, radius, height, progress, ModRenderTypes.haloWavyFog, haloTint,
            Math.toRadians(worldRotDeg));
        break;
      case WAVYPATTERN:
        renderWavyPattern(ms, radius, height, progress, ModRenderTypes.haloWavyPattern, haloTint,
            Math.toRadians(worldRotDeg));
        break;
      case BOTANIA:
      default:
        renderBotania(ms, radius, height, progress);
        break;
    }
  }

  // ── Shader-based backgrounds

  private static void renderClouds(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + .15D, width * 1.42D, bodyH * 1.34D, FADE_WIDTH * 2.35D,
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * .62F));

    double phase = RenderTick.total * .011D;
    float[] mist = HaloStylePalette.tint(HaloStylePalette.primary(HaloInterfaceStyle.CLOUDS, phase), tint, .72F, .035F);
    float[] bloom = HaloStylePalette.tint(HaloStylePalette.accent(HaloInterfaceStyle.CLOUDS, phase + .7D), tint, .82F, .045F);
    float[] frontPanel = HaloStylePalette.tint(new float[] {.04F, .24F, .48F}, tint, .86F, .055F);

    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  private static void renderSpace(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + .15D, width * 1.42D, bodyH * 1.34D, FADE_WIDTH * 2.35D,
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * .62F));

    double phase = RenderTick.total * .011D;
    float[] mist       = HaloStylePalette.tint(HaloStylePalette.primary(HaloInterfaceStyle.SPACE, phase), tint, .72F, .035F);
    float[] bloom      = HaloStylePalette.tint(HaloStylePalette.accent(HaloInterfaceStyle.SPACE, phase + .7D), tint, .82F, .045F);
    float[] frontPanel = HaloStylePalette.tint(new float[] {.02F, .06F, .35F}, tint, .86F, .055F);

    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  private static void renderFallingStars(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + .15D, width * 1.42D, bodyH * 1.34D, FADE_WIDTH * 2.35D,
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * .62F));

    double phase = RenderTick.total * .011D;
    float[] mist       = HaloStylePalette.tint(HaloStylePalette.primary(HaloInterfaceStyle.FALLINGSTARS, phase), tint, .72F, .035F);
    float[] bloom      = HaloStylePalette.tint(HaloStylePalette.accent(HaloInterfaceStyle.FALLINGSTARS, phase + .7D), tint, .82F, .045F);
    float[] frontPanel = HaloStylePalette.tint(new float[] {.35F, .28F, .02F}, tint, .86F, .055F);

    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  private static void renderLavaLamp(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + .15D, width * 1.42D, bodyH * 1.34D, FADE_WIDTH * 2.35D,
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * .62F));

    double phase = RenderTick.total * .011D;
    float[] mist       = HaloStylePalette.tint(HaloStylePalette.primary(HaloInterfaceStyle.LAVALAMP, phase), tint, .72F, .035F);
    float[] bloom      = HaloStylePalette.tint(HaloStylePalette.accent(HaloInterfaceStyle.LAVALAMP, phase + .7D), tint, .82F, .045F);
    float[] frontPanel = HaloStylePalette.tint(new float[] {.35F, .06F, .02F}, tint, .86F, .055F);

    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  private static void renderDepthMap(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + .15D, width * 1.42D, bodyH * 1.34D, FADE_WIDTH * 2.35D,
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * .62F));

    double phase = RenderTick.total * .011D;
    float[] mist       = HaloStylePalette.tint(HaloStylePalette.primary(HaloInterfaceStyle.DEPTHMAP, phase), tint, .72F, .035F);
    float[] bloom      = HaloStylePalette.tint(HaloStylePalette.accent(HaloInterfaceStyle.DEPTHMAP, phase + .7D), tint, .82F, .045F);
    float[] frontPanel = HaloStylePalette.tint(new float[] {.18F, .18F, .18F}, tint, .86F, .055F);

    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  private static void renderFoggyCloud(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + .15D, width * 1.42D, bodyH * 1.34D, FADE_WIDTH * 2.35D,
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * .62F));

    double phase = RenderTick.total * .011D;
    float[] mist       = HaloStylePalette.tint(HaloStylePalette.primary(HaloInterfaceStyle.FOGGYCLOUDS, phase), tint, .72F, .035F);
    float[] bloom      = HaloStylePalette.tint(HaloStylePalette.accent(HaloInterfaceStyle.FOGGYCLOUDS, phase + .7D), tint, .82F, .045F);
    float[] frontPanel = HaloStylePalette.tint(new float[] {.03F, .22F, .42F}, tint, .86F, .055F);

    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  private static void renderGlassLiquid(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + .15D, width * 1.42D, bodyH * 1.34D, FADE_WIDTH * 2.35D,
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * .62F));

    double phase = RenderTick.total * .011D;
    float[] mist       = HaloStylePalette.tint(HaloStylePalette.primary(HaloInterfaceStyle.GLASSLIQUID, phase), tint, .72F, .035F);
    float[] bloom      = HaloStylePalette.tint(HaloStylePalette.accent(HaloInterfaceStyle.GLASSLIQUID, phase + .7D), tint, .82F, .045F);
    float[] frontPanel = HaloStylePalette.tint(new float[] {.02F, .28F, .38F}, tint, .86F, .055F);

    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  private static void renderMetalCloud(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + .15D, width * 1.42D, bodyH * 1.34D, FADE_WIDTH * 2.35D,
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * .62F));

    double phase = RenderTick.total * .011D;
    float[] mist       = HaloStylePalette.tint(HaloStylePalette.primary(HaloInterfaceStyle.METALCLOUDS, phase), tint, .72F, .035F);
    float[] bloom      = HaloStylePalette.tint(HaloStylePalette.accent(HaloInterfaceStyle.METALCLOUDS, phase + .7D), tint, .82F, .045F);
    float[] frontPanel = HaloStylePalette.tint(new float[] {.20F, .20F, .22F}, tint, .86F, .055F);

    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  private static void renderSmokish(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + .15D, width * 1.42D, bodyH * 1.34D, FADE_WIDTH * 2.35D,
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * .62F));

    double phase = RenderTick.total * .011D;
    float[] mist       = HaloStylePalette.tint(HaloStylePalette.primary(HaloInterfaceStyle.SMOKISH, phase), tint, .72F, .035F);
    float[] bloom      = HaloStylePalette.tint(HaloStylePalette.accent(HaloInterfaceStyle.SMOKISH, phase + .7D), tint, .82F, .045F);
    float[] frontPanel = HaloStylePalette.tint(new float[] {.18F, .04F, .26F}, tint, .86F, .055F);

    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  private static void renderSplit(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + .15D, width * 1.42D, bodyH * 1.34D, FADE_WIDTH * 2.35D,
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * .62F));

    double phase = RenderTick.total * .011D;
    float[] mist       = HaloStylePalette.tint(HaloStylePalette.primary(HaloInterfaceStyle.SPLIT, phase), tint, .72F, .035F);
    float[] bloom      = HaloStylePalette.tint(HaloStylePalette.accent(HaloInterfaceStyle.SPLIT, phase + .7D), tint, .82F, .045F);
    float[] frontPanel = HaloStylePalette.tint(new float[] {.04F, .22F, .06F}, tint, .86F, .055F);

    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  private static void renderWavyFog(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + .15D, width * 1.42D, bodyH * 1.34D, FADE_WIDTH * 2.35D,
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * .62F));

    double phase = RenderTick.total * .011D;
    float[] mist       = HaloStylePalette.tint(HaloStylePalette.primary(HaloInterfaceStyle.WAVYFOG, phase), tint, .72F, .035F);
    float[] bloom      = HaloStylePalette.tint(HaloStylePalette.accent(HaloInterfaceStyle.WAVYFOG, phase + .7D), tint, .82F, .045F);
    float[] frontPanel = HaloStylePalette.tint(new float[] {.30F, .06F, .16F}, tint, .86F, .055F);

    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  private static void renderWavyPattern(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + .15D, width * 1.42D, bodyH * 1.34D, FADE_WIDTH * 2.35D,
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * .62F));

    double phase = RenderTick.total * .011D;
    float[] mist       = HaloStylePalette.tint(HaloStylePalette.primary(HaloInterfaceStyle.WAVYPATTERN, phase), tint, .72F, .035F);
    float[] bloom      = HaloStylePalette.tint(HaloStylePalette.accent(HaloInterfaceStyle.WAVYPATTERN, phase + .7D), tint, .82F, .045F);
    float[] frontPanel = HaloStylePalette.tint(new float[] {.32F, .18F, .02F}, tint, .86F, .055F);

    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  /** The three overlay panels used by every shader-based background (same sizing/parameters). */
  private static void renderSharedOverlayPanels(MatrixStack ms, double radius, double height, double progress,
      float[] mist, float[] bloom, float[] frontPanel) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);
    renderWavyLayer(ms, radius + .055D, width * 1.22D, bodyH * 1.16D, FADE_WIDTH * 1.35D,
      .09D, 2.1D, .022D, .2D, mist[0], mist[1], mist[2], (float) (progress * .1F));
    renderShiftedWavyLayer(ms, radius + .033D, width * 1.48D, bodyH * 1.36D, FADE_WIDTH * 1.85D,
      .13D, 2.75D, .031D, 1.5D, shimmerOffset(.018D, .95D), bloom[0], bloom[1], bloom[2], (float) (progress * .065F));
    RenderUtil.renderPartialHalo(ms, radius + .012D, width - FADE_WIDTH, bodyH, FADE_WIDTH,
      frontPanel[0], frontPanel[1], frontPanel[2], (float) (progress * .28F));
  }

  private static void renderShaderLayer(MatrixStack ms, RenderType type, double radius, double width, double height, double fadeWidth,
      double worldRotRad, float r, float g, float b, float alpha) {
    RenderSystem.setShaderColor(r, g, b, 1F);
    RenderUtil.renderPartialHaloShader(ms, type, radius, width - fadeWidth, height, fadeWidth,
        worldRotRad, 1F, 1F, 1F, alpha);
    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
  }

  private static void renderClassic(MatrixStack ms, double radius, double height, double progress) {
    RenderUtil.renderPartialHalo(ms, radius, visibleWidth(progress) - FADE_WIDTH, bodyHeight(height, progress), FADE_WIDTH, 0F, .7F, 1F, (float) (progress * .6F));
  }

  private static void renderMana(MatrixStack ms, double radius, double height, double progress) {
    double width = visibleWidth(progress);
    double bodyHeight = bodyHeight(height, progress);
    double phase = RenderTick.total * .012D;
    float[] mana = HaloStylePalette.primary(HaloInterfaceStyle.MANA, phase);
    float[] shimmer = HaloStylePalette.secondary(HaloInterfaceStyle.MANA, phase + .35D);
    float[] accent = HaloStylePalette.accent(HaloInterfaceStyle.MANA, phase + .7D);
    renderDynamicLighting(ms, radius, width, bodyHeight, progress, HaloInterfaceStyle.MANA, .7D);
    renderWavyLayer(ms, radius, width, bodyHeight, FADE_WIDTH, .08D, 3.5D, .045D, 0D, mana[0], mana[1], mana[2], (float) (progress * .58F));
    renderWavyLayer(ms, radius + .012D, width * .78D, bodyHeight * .5D, FADE_WIDTH * .75D, .04D, 5D, .06D, 1.2D, shimmer[0], shimmer[1], shimmer[2], (float) (progress * .34F));
    renderShiftedWavyLayer(ms, radius + .02D, width * .58D, bodyHeight * .22D, FADE_WIDTH * .5D, .025D, 7D, .08D, 2.4D, shimmerOffset(.035D, .75D), accent[0], accent[1], accent[2], (float) (progress * .25F));
    renderMotes(ms, radius, width, bodyHeight, progress, HaloInterfaceStyle.MANA, 16);
  }

  private static void renderCorporea(MatrixStack ms, double radius, double height, double progress) {
    double width = visibleWidth(progress);
    double bodyHeight = bodyHeight(height, progress);
    double phase = RenderTick.total * .011D + .4D;
    float[] corporea = HaloStylePalette.primary(HaloInterfaceStyle.CORPOREA, phase);
    float[] shimmer = HaloStylePalette.secondary(HaloInterfaceStyle.CORPOREA, phase + .35D);
    float[] accent = HaloStylePalette.accent(HaloInterfaceStyle.CORPOREA, phase + .7D);
    renderDynamicLighting(ms, radius, width, bodyHeight, progress, HaloInterfaceStyle.CORPOREA, 1.4D);
    renderWavyLayer(ms, radius, width, bodyHeight, FADE_WIDTH, .07D, 4D, .04D, 1.7D, corporea[0], corporea[1], corporea[2], (float) (progress * .56F));
    renderWavyLayer(ms, radius + .01D, width * .7D, bodyHeight * .42D, FADE_WIDTH * .7D, .035D, 6D, .065D, 2.6D, shimmer[0], shimmer[1], shimmer[2], (float) (progress * .32F));
    renderShiftedWavyLayer(ms, radius + .018D, width * .5D, bodyHeight * .24D, FADE_WIDTH * .45D, .03D, 7.5D, .07D, 3.1D, shimmerOffset(.04D, .7D), accent[0], accent[1], accent[2], (float) (progress * .3F));
    renderMotes(ms, radius, width, bodyHeight, progress, HaloInterfaceStyle.CORPOREA, 18);
  }

  private static void renderBotania(MatrixStack ms, double radius, double height, double progress) {
    double width = visibleWidth(progress);
    double bodyHeight = bodyHeight(height, progress);
    double phase = RenderTick.total * .012D;
    float[] mana = HaloStylePalette.primary(HaloInterfaceStyle.BOTANIA, phase);
    float[] corporea = HaloStylePalette.secondary(HaloInterfaceStyle.BOTANIA, phase + .35D);
    float[] bloom = HaloStylePalette.accent(HaloInterfaceStyle.BOTANIA, phase + .7D);

    renderDynamicLighting(ms, radius, width, bodyHeight, progress, HaloInterfaceStyle.BOTANIA, 2.2D);
    renderWavyLayer(ms, radius, width, bodyHeight, FADE_WIDTH, .085D, 3.5D, .04D, 0D, mana[0], mana[1], mana[2], (float) (progress * .46F));
    renderShiftedWavyLayer(ms, radius + .012D, width * .95D, bodyHeight * .9D, FADE_WIDTH, .055D, 5D, .052D, 1.4D, shimmerOffset(.03D, .6D), corporea[0], corporea[1], corporea[2], (float) (progress * .28F));
    renderWavyLayer(ms, radius + .018D, width * .64D, bodyHeight * .38D, FADE_WIDTH * .6D, .03D, 6D, .07D, 2.8D, bloom[0], bloom[1], bloom[2], (float) (progress * .28F));

    for (int i = 0; i < 3; i++) {
      double offset = movingStripeOffset(width, i);
      float alpha = (float) (progress * (.14D + wave(.06D, i) * .08D));
      renderShiftedWavyLayer(ms, radius + .024D, width * .055D, bodyHeight * 1.02D, width * .035D, .025D, 3D, .08D, i, offset, 1F, .95F, .65F, alpha);
    }
    renderMotes(ms, radius, width, bodyHeight, progress, HaloInterfaceStyle.BOTANIA, 28);
  }

  private static double visibleWidth(double progress) {
    return progress * MAX_WIDTH;
  }

  private static double bodyHeight(double height, double progress) {
    return height * progress + .05D;
  }

  private static float wave(double speed, double offset) {
    return (float) ((Math.sin(RenderTick.total * speed + offset) + 1D) * .5D);
  }

  private static double shimmerOffset(double speed, double amplitude) {
    return Math.sin(RenderTick.total * speed) * amplitude * .05D;
  }

  private static double movingStripeOffset(double width, int index) {
    double phase = (RenderTick.total * .006D + index / 3D) % 1D;
    return (phase - .5D) * width * 1.7D;
  }

  private static void renderDynamicLighting(MatrixStack ms, double radius, double width, double height, double progress, HaloInterfaceStyle style, double seed) {
    for (int layer = 0; layer < 3; layer++) {
      double phase = RenderTick.total * (.007D + layer * .002D) + seed + layer * .31D;
      float[] color = switch (layer) {
        case 0 -> HaloStylePalette.primary(style, phase);
        case 1 -> HaloStylePalette.secondary(style, phase);
        default -> HaloStylePalette.accent(style, phase);
      };
      double pulse = .55D + Math.sin(RenderTick.total * (.026D + layer * .006D) + seed + layer) * .45D;
      double layerWidth = width * (1.16D + layer * .14D);
      double layerHeight = height * (1.18D + layer * .2D);
      double layerRadius = radius + .05D + layer * .025D;
      double layerFade = FADE_WIDTH * (1.3D + layer * .24D);
      double offset = shimmerOffset(.024D + layer * .007D, .9D + layer * .25D);
      float alpha = (float) (progress * (.08D + pulse * .045D) / (layer + 1D));
      renderShiftedWavyLayer(ms, layerRadius, layerWidth, layerHeight, layerFade, .1D + layer * .03D, 2.4D + layer * .7D, .024D + layer * .012D, seed + layer, offset, color[0], color[1], color[2], alpha);
    }
  }

  private static void renderMotes(MatrixStack ms, double radius, double width, double height, double progress, HaloInterfaceStyle style, int count) {
    if (progress < .12D || width <= FADE_WIDTH * .35D) {
      return;
    }

    MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
    VertexConsumer buffer = buffers.getBuffer(ModRenderTypes.star);
    Matrix4f matrix = ms.getLast().getMatrix();
    double time = RenderTick.total;
    for (int index = 0; index < count; index++) {
      double seed = style.ordinal() * 83.19D + index * 17.37D;
      double travel = fract(seed * .071D + time * (.0026D + index % 5 * .00032D));
      double angle = (travel * 2D - 1D) * width * .95D;
      double wave = Math.sin(time * (.034D + index % 4 * .004D) + seed);
      double vertical = (fract(seed * .173D) * 2D - 1D) * height * .7D + wave * height * .18D;
      double edgeFade = edgeFade(angle, width);
      double twinkle = .55D + Math.sin(time * (.045D + index % 3 * .01D) + seed * .37D) * .45D;
      float alpha = (float) (progress * edgeFade * (.18D + twinkle * .34D));
      if (alpha <= .01F) {
        continue;
      }
      float[] color = HaloStylePalette.particle(style, travel + index * .13D + time * .006D);
      double sparkleRadius = radius + .035D + fract(seed * .29D) * .055D;
      double size = (.012D + fract(seed * .41D) * .024D) * (style == HaloInterfaceStyle.BOTANIA ? 1.12D : 1D);
      writeSparkle(buffer, matrix, sparkleRadius, angle, vertical, size, color[0], color[1], color[2], alpha);
    }
    buffers.endBatch(ModRenderTypes.star);
  }

  private static void writeSparkle(VertexConsumer buffer, Matrix4f matrix, double radius, double angle, double vertical, double size, float r, float g, float b, float alpha) {
    float centerX = (float) (Math.sin(angle) * radius);
    float centerY = (float) vertical;
    float centerZ = (float) (Math.cos(angle) * radius);
    float tangentX = (float) Math.cos(angle);
    float tangentZ = (float) -Math.sin(angle);
    float horizontalSize = (float) size;
    float verticalSize = (float) (size * 1.6D);

    writeSparkleTriangle(buffer, matrix, centerX, centerY, centerZ, centerX, centerY + verticalSize, centerZ, centerX + tangentX * horizontalSize, centerY, centerZ + tangentZ * horizontalSize, r, g, b, alpha);
    writeSparkleTriangle(buffer, matrix, centerX, centerY, centerZ, centerX + tangentX * horizontalSize, centerY, centerZ + tangentZ * horizontalSize, centerX, centerY - verticalSize, centerZ, r, g, b, alpha);
    writeSparkleTriangle(buffer, matrix, centerX, centerY, centerZ, centerX, centerY - verticalSize, centerZ, centerX - tangentX * horizontalSize, centerY, centerZ - tangentZ * horizontalSize, r, g, b, alpha);
    writeSparkleTriangle(buffer, matrix, centerX, centerY, centerZ, centerX - tangentX * horizontalSize, centerY, centerZ - tangentZ * horizontalSize, centerX, centerY + verticalSize, centerZ, r, g, b, alpha);
  }

  private static void writeSparkleTriangle(VertexConsumer buffer, Matrix4f matrix, float centerX, float centerY, float centerZ, float firstX, float firstY, float firstZ, float secondX, float secondY, float secondZ, float r, float g, float b, float alpha) {
    buffer.vertex(matrix, centerX, centerY, centerZ).color(r, g, b, alpha).endVertex();
    buffer.vertex(matrix, firstX, firstY, firstZ).color(r, g, b, 0F).endVertex();
    buffer.vertex(matrix, secondX, secondY, secondZ).color(r, g, b, 0F).endVertex();
  }

  private static double edgeFade(double angle, double width) {
    double distance = Math.min(width - angle, width + angle);
    return Math.max(0D, Math.min(1D, distance / Math.max(.001D, FADE_WIDTH)));
  }

  private static double fract(double value) {
    return value - Math.floor(value);
  }

  private static void renderShiftedWavyLayer(MatrixStack ms, double radius, double width, double height, double fadeWidth, double waveHeight, double waveFrequency, double waveSpeed, double waveOffset, double offset, float r, float g, float b, float alpha) {
    ms.push();
    ms.rotate(Vector3f.YP.rotation((float) offset));
    renderWavyLayer(ms, radius, width, height, fadeWidth, waveHeight, waveFrequency, waveSpeed, waveOffset, r, g, b, alpha);
    ms.pop();
  }

  private static void renderWavyLayer(MatrixStack ms, double radius, double width, double height, double fadeWidth, double waveHeight, double waveFrequency, double waveSpeed, double waveOffset, float r, float g, float b, float alpha) {
    RenderUtil.renderWavyPartialHalo(ms, radius, width - fadeWidth, height, fadeWidth, waveHeight, waveFrequency, waveSpeed, waveOffset, r, g, b, alpha);
  }
}