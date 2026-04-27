package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mcp.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.network.chat.Component;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.util.KeyboardHelper;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.util.*;
import vazkii.botania.client.core.handler.ClientTickHandler;

import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
public class AnimatedItemStack {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final float TEXT_SHADOW_Z_OFFSET = -.25F;
  private static final float TEXT_FOREGROUND_Z_OFFSET = -.5F;
  public Perlin noise = new Perlin();

  private final ItemStack stack;
  private boolean removed = false;
  private boolean forceRemove = false;
  private final Vec2i posi = new Vec2i();
  private final Vec2d pos = new Vec2d();
  private final Vec2d moveSpd = new Vec2d();
  private double fade;
  private double aniAmount;
  private double stackAniSpd;

  private final List<RequestResultAnimation> requestResultAnimations = new ArrayList<>();

  private boolean isNew = true;

  public AnimatedItemStack(ItemStack stack) {
    this.stack = stack;
    this.aniAmount = stack.getCount();
  }

  /**
   * Update the animation (Called every frame, no matter if the item is in the render area of not)
   * @return If the stack should be removed
   */
  public boolean update(double dt) {
    if (forceRemove) return true;
    if (removed && fade <= 0.01) return true;

    pos.add(moveSpd.copy().mul(dt));

    if (removed) {
      fade -= (Math.sin(fade * Math.PI / 2) + 1) * dt * .1;
      if (fade < 0)
        fade = 0;
    } else {
      fade += (Math.sin((1 - fade) * Math.PI / 2) + 1) * dt * .1;
      if (fade > 1)
        fade = 1;
    }

    aniAmount += stackAniSpd * dt;

    isNew = false;

    return false;
  }

  public void tick() {
    moveSpd.set(
        calcSpeed(pos.x, posi.x, moveSpd.x),
        calcSpeed(pos.y, posi.y, moveSpd.y)
    );

    stackAniSpd = Math.min(
        Math.abs((stack.getCount() - aniAmount) * .5) + .01,
        Math.abs(stackAniSpd) + Math.abs((stack.getCount() - aniAmount) * .2)
    ) * Math.signum(stack.getCount() - aniAmount);
  }

  private double calcSpeed(double current, double dest, double prevSpd) {
    return MathUtil.smoothMovingSpeed(current, dest, prevSpd, .05, .5, .01);
  }

  public void renderItem(MatrixStack ms) {
    ms.push();
    ms.rotate(Vector3f.YP.rotationDegrees(180));

    if (!removed) {
      float s = (float) fade;
      ms.scale(s, s, s);
      RenderUtil.renderFlatItem(ms, stack);
    } else {
      RenderUtil.applyStippling(fade, () -> RenderUtil.renderFlatItem(ms, stack));
    }

    ms.pop();
  }

  @SuppressWarnings("DuplicatedCode")
  public void renderAmount(MatrixStack ms, int color, MultiBufferSource.BufferSource buffers) {
    ms.push();
    float scale = (float) fade;
    ms.scale(scale, scale, scale);

    int orgAlpha = color >>> 24;
    int colorNoAlpha = color & 0x00FFFFFF;

    double spacing = 9;
    int numA = MathHelper.floor(aniAmount);
    int numB = MathHelper.ceil(aniAmount);
    double distA = numA - aniAmount;
    double distB = numB - aniAmount;
    String textA = TextHelper.formatBigNumber(numA, true);
    String textB = TextHelper.formatBigNumber(numB, true);

    ms.push();
    ms.translate(0, distA * spacing, 0);
    int colA = colorNoAlpha | (int) (orgAlpha * (1 - Math.abs(distA))) << 24;
    renderAmountText(ms, textA, colA, buffers);
    ms.pop();

    ms.push();
    ms.translate(0, distB * spacing, 0);
    int colB = colorNoAlpha | (int) (orgAlpha * (1 - Math.abs(distB))) << 24;
    renderAmountText(ms, textB, colB, buffers);
    ms.pop();

    ms.pop();
  }

  protected static void renderAmountText(MatrixStack ms, String text, int color, MultiBufferSource.BufferSource buffers) {
    int alpha = color >>> 24;
    color = (color & 0x00FFFFFF) | (int) (MathHelper.lerp(alpha / 255D, 5D, 249D)) << 24; //?????????????????

    ms.push();

    Font font = mc.font;

    ms.rotate(new Quaternion(Vector3f.XP, 180, true));
    ms.rotate(new Quaternion(Vector3f.YP, 180, true));
    double w = font.width(text);
    ms.translate(-w, 0, 0);

    RenderSystem.enableDepthTest();

    int shadeColor = (color & 16579836) >> 2 | color & -16777216;
    ms.push();
    ms.translate(0, 0, TEXT_SHADOW_Z_OFFSET);
    Matrix4f shadowMat = ms.getLast().getMatrix();
    font.drawInBatch(text, 1, 1, shadeColor, false, shadowMat, buffers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
    buffers.endBatch();
    ms.pop();

    ms.push();
    ms.translate(0, 0, TEXT_FOREGROUND_Z_OFFSET);
    Matrix4f mat = ms.getLast().getMatrix();
    font.drawInBatch(text, 0, 0, color, false, mat, buffers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
    buffers.endBatch();
    ms.pop();

    ms.pop();

  }

  public void renderRequestResultAnimations(MatrixStack ms, MultiBufferSource.BufferSource buffers) {
    ms.push();

    for (int i = requestResultAnimations.size() - 1; i >= 0; i--) {
      RequestResultAnimation animation = requestResultAnimations.get(i);
      if (animation.render(ms, buffers)) {
        requestResultAnimations.remove(i);
      }
    }

    ms.pop();
  }

  public void renderHud(GuiGraphics gui, float pt, Window window) {
    if (mc.player == null) return;

    if (KeyboardHelper.hasAltDown()) {
      List<Component> tooltip = stack.getTooltipLines(mc.player, mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
      int width = window.getGuiScaledWidth();
      int height = window.getGuiScaledHeight();
      gui.renderComponentTooltip(mc.font, tooltip, width / 2, height / 2);
    }
  }

  public void handleRequestResult(int successAmount) {
    requestResultAnimations.add(new RequestResultAnimation(successAmount));
  }

  public ItemStack getStack() {
    return stack;
  }

  public void fadeIn() {
    removed = false;
  }

  public boolean isNew() {
    return isNew;
  }

  public void remove() {
    removed = true;
    forceRemove = true;
  }

  public void removeWithAnimation() {
    remove();
    forceRemove = false;
  }

  public boolean isRemoved() {
    return removed;
  }

//  public boolean shouldDisplay() {
//    return shouldDisplay;
//  }

  public void setPos(int x, int y) {
    posi.set(x, y);
    pos.set(x, y);
  }

  public void moveTo(int x, int y) {
//    animationMove = new AnimationMove(animationLength, posi);
    posi.set(x, y);
  }

//  public Vec2i getPrevPosi() {
//    return animationMove == null ? posi : animationMove.getPrev();
//  }
//
//  public Vec2i getPosi() {
//    return posi;
//  }

  public Vec2d getPos() {
    return pos;
  }

  public void changeAmount(int amount, double animationLength) {
    stack.setCount(amount);
  }

  public void shrinkAmount(int amount, double animationLength) {
    changeAmount(Math.max(0, stack.getCount() - amount), animationLength);
  }
}

class RequestResultAnimation {
  private double progress;
  private final int amount;

  public RequestResultAnimation(int amount) {
    this.amount = amount;
  }

  public boolean render(MatrixStack ms, MultiBufferSource.BufferSource buffers) {
    progress += (RenderTick.delta * .05);
    if (progress >= 1) return true;

    ms.push();

    String text = "-" + amount;
    double w = Minecraft.getInstance().font.width(text);
    ms.translate(-w / 2, Math.sin(progress * Math.PI / 2) * 10, 0);
    int color = amount == 0 ? 0xFF0000 : 0x00FF00;
    color |= (int) ((1 - Math.sin(progress * Math.PI / 2)) * 255) << 24;
    AnimatedItemStack.renderAmountText(ms, text, color, buffers);

    ms.pop();

    return false;
  }
}
