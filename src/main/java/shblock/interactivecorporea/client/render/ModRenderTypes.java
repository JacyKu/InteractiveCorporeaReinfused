package shblock.interactivecorporea.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import shblock.interactivecorporea.IC;
import vazkii.botania.mixin.client.RenderTypeAccessor;

import javax.annotation.Nullable;

public class ModRenderTypes extends RenderStateShard {
    public static final RenderType halo = RenderTypeAccessor.create(
        IC.MODID + "_halo",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLE_STRIP,
        64,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(POSITION_COLOR_SHADER)
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    public static final RenderType craftingBg = RenderTypeAccessor.create(
        IC.MODID + "_crafting_bg",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.QUADS,
        16,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(POSITION_COLOR_TEX_SHADER)
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    public static final RenderType craftingSlotBg = RenderTypeAccessor.create(
        IC.MODID + "_crafting_slot_bg",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        16,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    public static final RenderType star = RenderTypeAccessor.create(
        IC.MODID + "_star",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLES,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(POSITION_COLOR_SHADER)
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    // ── Custom halo-shader render types (POSITION_COLOR_TEX + custom fsh) ──

    @Nullable public static ShaderInstance haloCloudShader;

    public static final RenderType haloCloud = RenderTypeAccessor.create(
        IC.MODID + "_halo_cloud",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.TRIANGLE_STRIP,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> haloCloudShader))
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    @Nullable public static ShaderInstance haloSpaceShader;

    public static final RenderType haloSpace = RenderTypeAccessor.create(
        IC.MODID + "_halo_space",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.TRIANGLE_STRIP,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> haloSpaceShader))
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    @Nullable public static ShaderInstance haloFallingStarsShader;

    public static final RenderType haloFallingStars = RenderTypeAccessor.create(
        IC.MODID + "_halo_fallingstars",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.TRIANGLE_STRIP,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> haloFallingStarsShader))
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    @Nullable public static ShaderInstance haloLavaLampShader;

    public static final RenderType haloLavaLamp = RenderTypeAccessor.create(
        IC.MODID + "_halo_lavalamp",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.TRIANGLE_STRIP,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> haloLavaLampShader))
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    @Nullable public static ShaderInstance haloDepthMapShader;

    public static final RenderType haloDepthMap = RenderTypeAccessor.create(
        IC.MODID + "_halo_depthmap",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.TRIANGLE_STRIP,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> haloDepthMapShader))
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    @Nullable public static ShaderInstance haloFoggyCloudShader;

    public static final RenderType haloFoggyCloud = RenderTypeAccessor.create(
        IC.MODID + "_halo_foggyclouds",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.TRIANGLE_STRIP,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> haloFoggyCloudShader))
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    @Nullable public static ShaderInstance haloGlassLiquidShader;

    public static final RenderType haloGlassLiquid = RenderTypeAccessor.create(
        IC.MODID + "_halo_glassliquid",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.TRIANGLE_STRIP,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> haloGlassLiquidShader))
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    @Nullable public static ShaderInstance haloMetalCloudShader;

    public static final RenderType haloMetalCloud = RenderTypeAccessor.create(
        IC.MODID + "_halo_metalclouds",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.TRIANGLE_STRIP,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> haloMetalCloudShader))
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    @Nullable public static ShaderInstance haloSmokishShader;

    public static final RenderType haloSmokish = RenderTypeAccessor.create(
        IC.MODID + "_halo_smokish",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.TRIANGLE_STRIP,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> haloSmokishShader))
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    @Nullable public static ShaderInstance haloSplitShader;

    public static final RenderType haloSplit = RenderTypeAccessor.create(
        IC.MODID + "_halo_split",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.TRIANGLE_STRIP,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> haloSplitShader))
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    @Nullable public static ShaderInstance haloWavyFogShader;

    public static final RenderType haloWavyFog = RenderTypeAccessor.create(
        IC.MODID + "_halo_wavyfog",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.TRIANGLE_STRIP,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> haloWavyFogShader))
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    @Nullable public static ShaderInstance haloWavyPatternShader;

    public static final RenderType haloWavyPattern = RenderTypeAccessor.create(
        IC.MODID + "_halo_wavypattern",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        VertexFormat.Mode.TRIANGLE_STRIP,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> haloWavyPatternShader))
            .setCullState(NO_CULL)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    private ModRenderTypes() {
        super("interactive_corporea_render_types", () -> {}, () -> {});
        throw new UnsupportedOperationException("Static helper class");
    }
}
