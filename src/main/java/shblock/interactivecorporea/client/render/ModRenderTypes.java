package shblock.interactivecorporea.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import shblock.interactivecorporea.IC;
import vazkii.botania.mixin.client.RenderTypeAccessor;

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

    private ModRenderTypes() {
        super("interactive_corporea_render_types", () -> {}, () -> {});
        throw new UnsupportedOperationException("Static helper class");
    }
}
