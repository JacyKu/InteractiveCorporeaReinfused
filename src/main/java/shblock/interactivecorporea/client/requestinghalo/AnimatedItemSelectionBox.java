package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.client.core.helper.RenderHelper;

import java.awt.*;

public class AnimatedItemSelectionBox {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final ResourceLocation ICON = new ResourceLocation(IC.MODID, "textures/ui/selection_box.png");
  private static final RenderType RENDER_TYPE = RenderHelper.getHaloLayer(ICON);

  private AnimatedItemStack target;
  private AnimatedItemStack lastTarget;
  private final Vec2d pos = new Vec2d();
  private float alpha;
  private float requestAnimationTime = -1F;
  private Runnable soundPlayer;

  public AnimatedItemSelectionBox(Runnable soundPlayer) {
    this.soundPlayer = soundPlayer;
  }

  public void setTarget(AnimatedItemStack target) {
    this.target = target;
  }

  public AnimatedItemStack getTarget() {
    return target;
  }

  public void update() {
    double spdA = .1F;
    double spdB = .01F;
    if (target == null) {
      alpha -= alpha * spdA + spdB;
      if (alpha < 0F)
        alpha = 0F;
    } else {
      alpha += (1 - alpha) * spdA + spdB;
      if (alpha > 1F)
        alpha = 1F;
    }

    if (target != null) {
      if (target != lastTarget) {
        soundPlayer.run();
      }

      Vec2d targetPos = target.getPos();
      double spdModifier = Math.sin(MathHelper.clamp(pos.distanceTo(targetPos), 0F, .5F) * Math.PI);
      spdModifier = MathHelper.clamp(spdModifier, 0.1F, 0.5F);
      spdModifier *= RenderTick.delta;
      pos.add(
          (targetPos.x - pos.x) * spdModifier,
          (targetPos.y - pos.y) * spdModifier
      );

      if (target.isRemoved()) {
        target = null;
      }
    }
    lastTarget = target;
  }

  public Vec2d getPos() {
    return pos;
  }

  public void render(MatrixStack ms) {
    ms.push();
    ms.translate(0, 0, -.1);
    MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
    VertexConsumer buffer = buffers.getBuffer(RENDER_TYPE);
    Color color = Color.getHSBColor((float) (RenderTick.total  / 200F),1F, 1F);
    float r = color.getRed() / 255F;
    float g = color.getGreen() / 255F;
    float b = color.getBlue() / 255F;
    if (requestAnimationTime > 0F) {
      requestAnimationTime -= RenderTick.delta / 5F;
      float aniScale = .25F;
      ms.scale(
          (float) (Math.sin(requestAnimationTime * Math.PI * 2F + Math.PI * 2F) * aniScale + 1F),
          (float) (-Math.sin(requestAnimationTime * Math.PI * 2F) * aniScale + 1F),
          1F
      );
    }
    Matrix4f matrix = ms.getLast().getMatrix();
    buffer.vertex(matrix, -.5F, -.5F, 0F).color(r, g, b, alpha).uv(0F, 0F).endVertex();
    buffer.vertex(matrix, -.5F, .5F, 0F).color(r, g, b, alpha).uv(0F, 1F).endVertex();
    buffer.vertex(matrix, .5F, .5F, 0F).color(r, g, b, alpha).uv(1F, 1F).endVertex();
    buffer.vertex(matrix, .5F, -.5F, 0F).color(r, g, b, alpha).uv(1F, 0F).endVertex();
    buffers.endBatch(RENDER_TYPE);
    ms.pop();
  }

  /**
   * called when the client send the requesting packet to the server (not when the request has been success)
   */
  public void playRequestAnimation() {
    requestAnimationTime = 1F;
  }
}
