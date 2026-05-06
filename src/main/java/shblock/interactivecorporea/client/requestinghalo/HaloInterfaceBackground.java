package shblock.interactivecorporea.client.requestinghalo;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.ModConfig;
import shblock.interactivecorporea.client.render.DeferredWorldRenderQueue;
import shblock.interactivecorporea.client.render.ModRenderTypes;
import shblock.interactivecorporea.client.render.OculusCompat;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.render.shader.RawShaderProgram;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.item.HaloInterfaceStyle;

public final class HaloInterfaceBackground {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final long SHADER_STATUS_LOG_INTERVAL_MS = 2000L;
  private static final Minecraft mc = Minecraft.getInstance();
  private static final double FADE_WIDTH = .3D;
  private static final double MAX_WIDTH = Math.PI * .25D;
  private static final double SHADER_OVERFLOW = .12D;
  private static final double SHADER_UV_U_SCALE = 3.0D;
  private static final double SHADER_LAYER_RADIUS_OFFSET = .15D;
  private static final float  SHADER_LAYER_ALPHA          = .62F;
  private static final double OVERLAY_PHASE_SPEED         = .011D;
  private static final double OVERLAY_BLOOM_PHASE_OFFSET  = .7D;
  private static final float  OVERLAY_MIST_TINT           = .72F;
  private static final float  OVERLAY_MIST_NOISE          = .035F;
  private static final float  OVERLAY_BLOOM_TINT          = .82F;
  private static final float  OVERLAY_BLOOM_NOISE         = .045F;
  private static final float  OVERLAY_FRONT_TINT          = .86F;
  private static final float  OVERLAY_FRONT_NOISE         = .055F;
  private static final Set<String> loggedMissingShaders = new HashSet<>();
  private static final Set<String> loggedDisabledStyles = new HashSet<>();
  private static final Map<RenderType, RawShaderProgram> RAW_HALO_SHADERS = createRawHaloShaders();
  private static long nextShaderStatusLogAt;

  private HaloInterfaceBackground() {
  }

  private static Map<RenderType, RawShaderProgram> createRawHaloShaders() {
    Map<RenderType, RawShaderProgram> shaders = new IdentityHashMap<>();
    registerRawHaloShader(shaders, ModRenderTypes.haloCloud, "halo_clouds");
    registerRawHaloShader(shaders, ModRenderTypes.haloSpace, "halo_space");
    registerRawHaloShader(shaders, ModRenderTypes.haloFallingStars, "halo_fallingstars");
    registerRawHaloShader(shaders, ModRenderTypes.haloLavaLamp, "halo_lavalamp");
    registerRawHaloShader(shaders, ModRenderTypes.haloDepthMap, "halo_depthmap");
    registerRawHaloShader(shaders, ModRenderTypes.haloFoggyCloud, "halo_foggyclouds");
    registerRawHaloShader(shaders, ModRenderTypes.haloGlassLiquid, "halo_glassliquid");
    registerRawHaloShader(shaders, ModRenderTypes.haloMetalCloud, "halo_metalclouds");
    registerRawHaloShader(shaders, ModRenderTypes.haloSmokish, "halo_smokish");
    registerRawHaloShader(shaders, ModRenderTypes.haloSplit, "halo_split");
    registerRawHaloShader(shaders, ModRenderTypes.haloWavyFog, "halo_wavyfog");
    registerRawHaloShader(shaders, ModRenderTypes.haloWavyPattern, "halo_wavypattern");
    return shaders;
  }

  private static void registerRawHaloShader(Map<RenderType, RawShaderProgram> shaders, RenderType type, String shaderName) {
    shaders.put(type, new RawShaderProgram(
        ResourceLocation.fromNamespaceAndPath(IC.MODID, "shaders/core/halo_raw.vsh"),
        ResourceLocation.fromNamespaceAndPath(IC.MODID, "shaders/core/" + shaderName + ".fsh"),
        null,
        null
    ));
  }

  public static void render(MatrixStack ms, double radius, double height, double progress,
      HaloInterfaceStyle style, float[] haloTint, double worldRotDeg) {
    if (!ModConfig.CLIENT.enableHaloShaders.get() && style.isShaderStyle()) {
      logDisabledShaderStyle(style);
      style = HaloInterfaceStyle.CLASSIC;
    }
    renderInternal(ms, radius, height, progress, style, haloTint, worldRotDeg);
  }

  private static void renderInternal(MatrixStack ms, double radius, double height, double progress,
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

  private static void renderShaderStyle(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad, HaloInterfaceStyle style, float[] frontPanelBase) {
    double sw = shaderWidth(progress);
    double bodyH = bodyHeight(height, progress);

    renderShaderLayer(ms, type, radius + SHADER_LAYER_RADIUS_OFFSET, Math.max(0D, sw - FADE_WIDTH), bodyH, Math.min(FADE_WIDTH, sw),
      worldRotRad, tint[0], tint[1], tint[2], (float) (progress * SHADER_LAYER_ALPHA));

    double phase = RenderTick.total * OVERLAY_PHASE_SPEED;
    float[] mist       = HaloStylePalette.tint(HaloStylePalette.primary(style, phase), tint, OVERLAY_MIST_TINT, OVERLAY_MIST_NOISE);
    float[] bloom      = HaloStylePalette.tint(HaloStylePalette.accent(style, phase + OVERLAY_BLOOM_PHASE_OFFSET), tint, OVERLAY_BLOOM_TINT, OVERLAY_BLOOM_NOISE);
    float[] frontPanel = HaloStylePalette.tint(frontPanelBase, tint, OVERLAY_FRONT_TINT, OVERLAY_FRONT_NOISE);
    renderSharedOverlayPanels(ms, radius, height, progress, mist, bloom, frontPanel);
  }

  private static void renderClouds(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    renderShaderStyle(ms, radius, height, progress, type, tint, worldRotRad,
        HaloInterfaceStyle.CLOUDS, new float[] {.04F, .24F, .48F});
  }

  private static void renderSpace(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    renderShaderStyle(ms, radius, height, progress, type, tint, worldRotRad,
        HaloInterfaceStyle.SPACE, new float[] {.02F, .06F, .35F});
  }

  private static void renderFallingStars(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    renderShaderStyle(ms, radius, height, progress, type, tint, worldRotRad,
        HaloInterfaceStyle.FALLINGSTARS, new float[] {.35F, .28F, .02F});
  }

  private static void renderLavaLamp(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    renderShaderStyle(ms, radius, height, progress, type, tint, worldRotRad,
        HaloInterfaceStyle.LAVALAMP, new float[] {.35F, .06F, .02F});
  }

  private static void renderDepthMap(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    renderShaderStyle(ms, radius, height, progress, type, tint, worldRotRad,
        HaloInterfaceStyle.DEPTHMAP, new float[] {.18F, .18F, .18F});
  }

  private static void renderFoggyCloud(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    renderShaderStyle(ms, radius, height, progress, type, tint, worldRotRad,
        HaloInterfaceStyle.FOGGYCLOUDS, new float[] {.03F, .22F, .42F});
  }

  private static void renderGlassLiquid(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    renderShaderStyle(ms, radius, height, progress, type, tint, worldRotRad,
        HaloInterfaceStyle.GLASSLIQUID, new float[] {.02F, .28F, .38F});
  }

  private static void renderMetalCloud(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    renderShaderStyle(ms, radius, height, progress, type, tint, worldRotRad,
        HaloInterfaceStyle.METALCLOUDS, new float[] {.20F, .20F, .22F});
  }

  private static void renderSmokish(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    renderShaderStyle(ms, radius, height, progress, type, tint, worldRotRad,
        HaloInterfaceStyle.SMOKISH, new float[] {.18F, .04F, .26F});
  }

  private static void renderSplit(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    renderShaderStyle(ms, radius, height, progress, type, tint, worldRotRad,
        HaloInterfaceStyle.SPLIT, new float[] {.04F, .22F, .06F});
  }

  private static void renderWavyFog(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    renderShaderStyle(ms, radius, height, progress, type, tint, worldRotRad,
        HaloInterfaceStyle.WAVYFOG, new float[] {.30F, .06F, .16F});
  }

  private static void renderWavyPattern(MatrixStack ms, double radius, double height, double progress,
      RenderType type, float[] tint, double worldRotRad) {
    renderShaderStyle(ms, radius, height, progress, type, tint, worldRotRad,
        HaloInterfaceStyle.WAVYPATTERN, new float[] {.32F, .18F, .02F});
  }

  private static void renderSharedOverlayPanels(MatrixStack ms, double radius, double height, double progress,
      float[] mist, float[] bloom, float[] frontPanel) {
    double width = visibleWidth(progress);
    double bodyH = bodyHeight(height, progress);
    renderWavyLayer(ms, radius + .055D, width * 1.22D, bodyH * 1.16D, FADE_WIDTH * 1.35D,
      .09D, 2.1D, .022D, .2D, mist[0], mist[1], mist[2], (float) (progress * .1F));
    renderShiftedWavyLayer(ms, radius + .033D, width * 1.48D, bodyH * 1.36D, FADE_WIDTH * 1.85D,
      .13D, 2.75D, .031D, 1.5D, shimmerOffset(.018D, .95D), bloom[0], bloom[1], bloom[2], (float) (progress * .065F));
    renderWavyLayer(ms, radius + .012D, width, bodyH, FADE_WIDTH,
      .055D, 1.6D, .016D, .9D, frontPanel[0], frontPanel[1], frontPanel[2], (float) (progress * .28F));
  }

  private static void renderShaderLayer(MatrixStack ms, RenderType type, double radius, double width, double height, double fadeWidth,
      double worldRotRad, float r, float g, float b, float alpha) {
    double fullWidth = width;
    if (fullWidth <= 0D) {
      return;
    }

    RawShaderProgram rawShader = RAW_HALO_SHADERS.get(type);
    if (rawShader != null && rawShader.isLoaded()) {
      renderRawShaderLayer(ms, rawShader, radius, fullWidth, height, fadeWidth, worldRotRad, r, g, b, alpha);
      return;
    }

    ShaderInstance shader = ModRenderTypes.getHaloShader(type);
    if (shader == null) {
      logMissingShader(type);
      return;
    }

    renderMinecraftShaderLayer(ms, type, shader, radius, fullWidth, height, fadeWidth, worldRotRad, r, g, b, alpha);
  }

  private static void renderRawShaderLayer(MatrixStack ms, RawShaderProgram shader, double radius, double fullWidth, double height,
      double fadeWidth, double worldRotRad, float r, float g, float b, float alpha) {
    org.joml.Matrix4f poseSnapshot = new org.joml.Matrix4f(ms.getLast().getMatrix());
    org.joml.Matrix4f modelView = new org.joml.Matrix4f(RenderSystem.getModelViewMatrix());
    org.joml.Matrix4f projection = new org.joml.Matrix4f(RenderSystem.getProjectionMatrix());
    DeferredRawShaderDraw draw = new DeferredRawShaderDraw(
        shader, poseSnapshot, modelView, projection,
        radius, fullWidth, height, fadeWidth, worldRotRad, r, g, b, alpha
    );
    DeferredWorldRenderQueue.enqueue(() -> executeRawShaderDraw(draw));
  }

  private static void executeRawShaderDraw(DeferredRawShaderDraw draw) {
    Tesselator tessellator = Tesselator.getInstance();
    BufferBuilder buffer = tessellator.getBuilder();
    RawShaderProgram shader = draw.shader;
    double angleStep = ModConfig.getHaloShaderAngleStep();

    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.disableCull();
    RenderSystem.depthMask(false);

    try {
      shader.use();
      shader.setUniformMatrix4f("ModelViewMat", draw.modelView);
      shader.setUniformMatrix4f("ProjMat", draw.projection);
      shader.setUniform1f("GameTime", (float) (RenderTick.total / 24000D));
      shader.setUniform4f("ColorModulator", draw.r, draw.g, draw.b, 1F);
      logRawShaderDraw(shader, draw.radius, draw.fullWidth, draw.height, draw.fadeWidth, draw.worldRotRad, draw.alpha);

      org.joml.Matrix4f matrix = draw.pose;

      buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR_TEX);
      double visibleHalfWidth = draw.fullWidth + draw.fadeWidth;
      for (double angle = -visibleHalfWidth; angle < visibleHalfWidth; angle += angleStep) {
        float xp = (float) (Math.sin(angle) * draw.radius);
        float zp = (float) (Math.cos(angle) * draw.radius);

        double minDistToEdge = Math.min(Math.abs(visibleHalfWidth - angle), Math.abs(-visibleHalfWidth - angle));
        float currentAlpha = draw.alpha;
        if (minDistToEdge < draw.fadeWidth) {
          currentAlpha *= (float) Math.sin((minDistToEdge / draw.fadeWidth) * (Math.PI / 2));
        }

        float u = (float) ((angle - draw.worldRotRad) / (2.0 * Math.PI) * SHADER_UV_U_SCALE);
        buffer.vertex(matrix, xp, (float) (-draw.height), zp).color(1F, 1F, 1F, currentAlpha).uv(u, 0F).endVertex();
        buffer.vertex(matrix, xp, (float) draw.height, zp).color(1F, 1F, 1F, currentAlpha).uv(u, 1F).endVertex();
      }
      BufferUploader.draw(buffer.end());
    } finally {
      shader.release();
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
    }
  }

  private static final class DeferredRawShaderDraw {
    final RawShaderProgram shader;
    final org.joml.Matrix4f pose;
    final org.joml.Matrix4f modelView;
    final org.joml.Matrix4f projection;
    final double radius;
    final double fullWidth;
    final double height;
    final double fadeWidth;
    final double worldRotRad;
    final float r;
    final float g;
    final float b;
    final float alpha;

    DeferredRawShaderDraw(RawShaderProgram shader, org.joml.Matrix4f pose,
        org.joml.Matrix4f modelView, org.joml.Matrix4f projection,
        double radius, double fullWidth, double height, double fadeWidth, double worldRotRad,
        float r, float g, float b, float alpha) {
      this.shader = shader;
      this.pose = pose;
      this.modelView = modelView;
      this.projection = projection;
      this.radius = radius;
      this.fullWidth = fullWidth;
      this.height = height;
      this.fadeWidth = fadeWidth;
      this.worldRotRad = worldRotRad;
      this.r = r;
      this.g = g;
      this.b = b;
      this.alpha = alpha;
    }
  }

  private static void renderMinecraftShaderLayer(MatrixStack ms, RenderType type, ShaderInstance shader, double radius, double fullWidth,
      double height, double fadeWidth, double worldRotRad, float r, float g, float b, float alpha) {

    Tesselator tessellator = Tesselator.getInstance();
    BufferBuilder buffer = tessellator.getBuilder();
    Matrix4f matrix = ms.getLast().getMatrix();
    double angleStep = ModConfig.getHaloShaderAngleStep();

    OculusCompat.withoutGbufferOverride(() -> {
      RenderSystem.setShaderColor(r, g, b, 1F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(() -> shader);
      logShaderDraw(type, shader, radius, fullWidth, height, fadeWidth, worldRotRad, alpha);

      try {
        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR_TEX);
        double visibleHalfWidth = fullWidth + fadeWidth;
        for (double angle = -visibleHalfWidth; angle < visibleHalfWidth; angle += angleStep) {
          float xp = (float) (Math.sin(angle) * radius);
          float zp = (float) (Math.cos(angle) * radius);

          double minDistToEdge = Math.min(Math.abs(visibleHalfWidth - angle), Math.abs(-visibleHalfWidth - angle));
          float currentAlpha = alpha;
          if (minDistToEdge < fadeWidth) {
            currentAlpha *= (float) Math.sin((minDistToEdge / fadeWidth) * (Math.PI / 2));
          }

          float u = (float) ((angle - worldRotRad) / (2.0 * Math.PI) * SHADER_UV_U_SCALE);
          buffer.vertex(matrix, xp, (float) (-height), zp).color(1F, 1F, 1F, currentAlpha).uv(u, 0F).endVertex();
          buffer.vertex(matrix, xp, (float) height, zp).color(1F, 1F, 1F, currentAlpha).uv(u, 1F).endVertex();
        }
        tessellator.end();
      } finally {
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
      }
    });
  }

  private static void logMissingShader(RenderType type) {
    if (!ModConfig.isShaderDebugEnabled()) {
      return;
    }
    String key = String.valueOf(type);
    if (loggedMissingShaders.add(key)) {
      LOGGER.warn("[HaloShaderDebug] No ShaderInstance resolved for halo render type {}.", key);
    }
  }

  private static void logDisabledShaderStyle(HaloInterfaceStyle style) {
    if (!ModConfig.isShaderDebugEnabled()) {
      return;
    }
    String key = style.name();
    if (loggedDisabledStyles.add(key)) {
      LOGGER.info("[HaloShaderDebug] Halo shader style {} was requested but client halo shaders are disabled; falling back to CLASSIC.", key);
    }
  }

  private static void logShaderDraw(RenderType type, ShaderInstance shader, double radius, double width, double height,
      double fadeWidth, double worldRotRad, float alpha) {
    if (!ModConfig.isShaderDebugEnabled()) {
      return;
    }
    long now = System.currentTimeMillis();
    if (now < nextShaderStatusLogAt) {
      return;
    }
    nextShaderStatusLogAt = now + SHADER_STATUS_LOG_INTERVAL_MS;
    LOGGER.info(
        "[HaloShaderDebug] Drawing halo shader layer. type={}, shaderClass={}, radius={}, width={}, height={}, fadeWidth={}, worldRotRad={}, alpha={}, renderTick={}",
        type,
        shader.getClass().getName(),
        String.format("%.3f", radius),
        String.format("%.3f", width),
        String.format("%.3f", height),
        String.format("%.3f", fadeWidth),
        String.format("%.3f", worldRotRad),
        String.format("%.3f", alpha),
        String.format("%.3f", RenderTick.total)
    );
  }

  private static void logRawShaderDraw(RawShaderProgram shader, double radius, double width, double height,
      double fadeWidth, double worldRotRad, float alpha) {
    if (!ModConfig.isShaderDebugEnabled()) {
      return;
    }
    long now = System.currentTimeMillis();
    if (now < nextShaderStatusLogAt) {
      return;
    }
    nextShaderStatusLogAt = now + SHADER_STATUS_LOG_INTERVAL_MS;
    LOGGER.info(
        "[HaloShaderDebug] Drawing raw halo shader layer. shaderClass={}, radius={}, width={}, height={}, fadeWidth={}, worldRotRad={}, alpha={}, renderTick={}",
        shader.getClass().getName(),
        String.format("%.3f", radius),
        String.format("%.3f", width),
        String.format("%.3f", height),
        String.format("%.3f", fadeWidth),
        String.format("%.3f", worldRotRad),
        String.format("%.3f", alpha),
        String.format("%.3f", RenderTick.total)
    );
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

  private static double shaderWidth(double progress) {
    return visibleWidth(progress) + SHADER_OVERFLOW * progress;
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