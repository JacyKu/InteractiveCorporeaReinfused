package shblock.interactivecorporea.client.requestinghalo.crafting;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.core.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import shblock.interactivecorporea.client.jei.DummyTransferringGui;
import shblock.interactivecorporea.client.render.ModRenderTypes;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.requestinghalo.AnimatedItemStack;
import shblock.interactivecorporea.client.requestinghalo.HaloPickedItem;
import shblock.interactivecorporea.client.requestinghalo.HaloStylePalette;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.item.HaloInterfaceStyle;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.network.CPacketChangeStackInHaloCraftingSlot;
import shblock.interactivecorporea.common.network.CPacketDoCraft;
import shblock.interactivecorporea.common.network.CPacketSetHaloCraftingShadowSlot;
import shblock.interactivecorporea.common.network.ModPacketHandler;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.MathUtil;
import shblock.interactivecorporea.common.util.TextHelper;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.common.core.helper.Vector3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class HaloCraftingInterface {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final Vector3 Y_AXIS = new Vector3(0, 1, 0);
  private static final Vector3 X_AXIS = new Vector3(1, 0, 0);
  private static final double GRID_TILT_DEGREES = 18D;
  private static final double GRID_TILT_RADIANS = Math.toRadians(GRID_TILT_DEGREES);
  private static final float PANEL_CORNER_RADIUS = .22F;
  private static final float PANEL_FADE_WIDTH = .24F;
  private static final float PANEL_WAVE_AMPLITUDE = .032F;
  private static final float PANEL_WAVE_FREQUENCY = 5.5F;
  private static final float PANEL_WAVE_SPEED = .065F;
  private static final int PANEL_SEGMENTS = 18;
  private static final float SLOT_CORNER_RADIUS = .2F;
  private static final float SLOT_FADE_WIDTH = .2F;
  private static final int SLOT_SEGMENTS = 8;
  private static final float CRAFT_BUTTON_CENTER_X = -1.34F;
  private static final float CRAFT_BUTTON_HALF_WIDTH = .36F;
  private static final float CRAFT_BUTTON_HALF_HEIGHT = .39F;
  private static final float CRAFT_BUTTON_BORDER = .04F;
  private static final float CRAFT_BUTTON_CORNER_RADIUS = .15F;
  private static final float CRAFT_BUTTON_FADE_WIDTH = .08F;
  private static final int CRAFT_BUTTON_SEGMENTS = 10;
  private static final MultiBufferSource.BufferSource TEXT_BUFFERS = MultiBufferSource.immediate(new BufferBuilder(64));

  public final CISlotPointer haloItemSlot;
  public ItemStack haloStack;

  private double rotation = 0;
  private double targetRotation = 0;
  private double rotationSpd = 0;
  private double pos = 0;
  private double size = 1;

  private static final Vec2d NOT_POINTING = new Vec2d(Double.NaN, Double.NaN);

  private Vec2d pointingLocalPos = NOT_POINTING;
  private boolean showCraftButton = true;

  private double mouseOverAnimation = 0;
  private double craftButtonHoverAnimation = 0;

  private final CraftingInterfaceSlot[] slots = new CraftingInterfaceSlot[9];
  private CraftingRecipe currentRecipe = null;
  private ItemStack currentOutput = ItemStack.EMPTY;
  private NonNullList<ItemStack> currentRemainingItems = NonNullList.withSize(9, ItemStack.EMPTY);
  private double craftingOutputAnimation = 0;

  public HaloCraftingInterface(CISlotPointer haloItemSlot, ItemStack haloStack) {
    this.haloItemSlot = haloItemSlot;
    this.haloStack = haloStack;

    for (int i = 0; i < 9; i++) {
      slots[i] = new CraftingInterfaceSlot(this, i, ItemRequestingHalo.getShadowStackInCraftingSlot(haloStack, i));
    }
  }

  public void render(MatrixStack ms, double openCloseAnimation) {
    rotation += rotationSpd * RenderTick.delta;

    if (isPointingAtInterface()) {
      mouseOverAnimation += RenderTick.delta / 10;
    } else {
      mouseOverAnimation -= RenderTick.delta / 10;
    }
    mouseOverAnimation = MathHelper.clamp(mouseOverAnimation, 0, 1);

    if (showCraftButton && isPointingAtCraftButton()) {
      craftButtonHoverAnimation += RenderTick.delta / 7;
    } else {
      craftButtonHoverAnimation -= RenderTick.delta / 7;
    }
    craftButtonHoverAnimation = MathHelper.clamp(craftButtonHoverAnimation, 0, 1);

    if (!currentOutput.isEmpty()) {
      craftingOutputAnimation += RenderTick.delta / 8;
      if (craftingOutputAnimation > 1)
        craftingOutputAnimation = 1;
    } else {
      craftingOutputAnimation = 0;
    }

    ms.push();

    ms.rotate(Vector3f.YP.rotation((float) -rotation));
    ms.translate(0, 0, pos);
    ms.rotate(Vector3f.XP.rotationDegrees((float) -GRID_TILT_DEGREES));
    float scale = (float) (size * openCloseAnimation);
    ms.scale(scale, scale, scale);

    double mouseOverFactor = (1 - Math.cos(mouseOverAnimation * Math.PI)) / 2;
    float[] plateColor = getPlateColor((float) mouseOverFactor);
    float[] glowColor = getGlowColor();
    float a = (float) (.18F + mouseOverFactor * .08F);
    float s = (float) (1 + mouseOverFactor * .3);
    renderPanel(ms, s * 1.05F, s * 1.05F, PANEL_CORNER_RADIUS * 1.1F, PANEL_FADE_WIDTH * 1.25F, PANEL_SEGMENTS,
        PANEL_WAVE_AMPLITUDE * 1.35F, PANEL_WAVE_FREQUENCY * .85F, PANEL_WAVE_SPEED * .9F, .85F,
        glowColor[0], glowColor[1], glowColor[2], a * .18F);
    renderPanel(ms, s, s, PANEL_CORNER_RADIUS, PANEL_FADE_WIDTH, PANEL_SEGMENTS,
        PANEL_WAVE_AMPLITUDE, PANEL_WAVE_FREQUENCY, PANEL_WAVE_SPEED, 0F,
        plateColor[0], plateColor[1], plateColor[2], a);
    if (showCraftButton) {
      renderCraftButton(ms);
    }

    ms.push();
    ms.translate(0, .01, 0);
    for (CraftingInterfaceSlot slot : slots) {
      slot.render(ms, pointingLocalPos);
    }
    ms.pop();

    ms.pop();
  }

  public void snapRotation(double rotation) {
    this.rotation = rotation;
    this.targetRotation = rotation;
    this.rotationSpd = 0;
  }

  public void setShowCraftButton(boolean showCraftButton) {
    this.showCraftButton = showCraftButton;
  }

  public float[] getSlotColor(float hoverFactor) {
    float phase = (float) (RenderTick.total * .012D);
    float[] secondary = getStyledColor(false, phase + .35D);
    float[] accent = getStyledColor(true, phase + .7D);
    return blendColors(secondary, accent, .22F + hoverFactor * .48F);
  }

  private void renderCraftButton(MatrixStack ms) {
    float hoverFactor = (float) ((1 - Math.cos(craftButtonHoverAnimation * Math.PI)) / 2);
    boolean craftable = currentRecipe != null;
    float[] plateBase = getPlateColor(Math.max(.1F, hoverFactor * .8F));
    float[] accent = getStyledColor(true, RenderTick.total * .014D + .2D);
    float[] slotBase = getSlotColor(Math.max(.15F, hoverFactor * .85F));
    float[] glowColor = blendColors(plateBase, accent, .46F + hoverFactor * .16F);
    float[] fillColor = craftable
      ? blendColors(plateBase, slotBase, .28F + hoverFactor * .12F)
      : blendColors(plateBase, slotBase, .12F + hoverFactor * .06F);
    float glowAlpha = craftable ? .34F + hoverFactor * .1F : .16F + hoverFactor * .05F;
    float fillAlpha = craftable ? .52F + hoverFactor * .08F : .24F + hoverFactor * .05F;

    ms.push();
    ms.translate(CRAFT_BUTTON_CENTER_X, .014D + hoverFactor * .008D, 0D);
    renderPanel(ms,
      CRAFT_BUTTON_HALF_WIDTH + CRAFT_BUTTON_BORDER,
      CRAFT_BUTTON_HALF_HEIGHT + CRAFT_BUTTON_BORDER,
      CRAFT_BUTTON_CORNER_RADIUS * 1.1F,
      CRAFT_BUTTON_FADE_WIDTH,
      CRAFT_BUTTON_SEGMENTS,
      PANEL_WAVE_AMPLITUDE * .55F,
      PANEL_WAVE_FREQUENCY,
      PANEL_WAVE_SPEED,
      .35F,
      glowColor[0], glowColor[1], glowColor[2], glowAlpha);
    ms.push();
    ms.translate(0D, .004D + hoverFactor * .006D, 0D);
    renderPanel(ms,
      CRAFT_BUTTON_HALF_WIDTH,
      CRAFT_BUTTON_HALF_HEIGHT,
      CRAFT_BUTTON_CORNER_RADIUS,
      CRAFT_BUTTON_FADE_WIDTH,
      CRAFT_BUTTON_SEGMENTS,
      PANEL_WAVE_AMPLITUDE * .35F,
      PANEL_WAVE_FREQUENCY,
      PANEL_WAVE_SPEED,
      0F,
      fillColor[0], fillColor[1], fillColor[2], fillAlpha);
    if (craftable && !currentOutput.isEmpty()) {
      renderCraftButtonOutput(ms, hoverFactor);
    }
    ms.pop();
    ms.pop();
  }

  private void renderCraftButtonOutput(MatrixStack ms, float hoverFactor) {
    double outputFactor = (1 - Math.cos(craftingOutputAnimation * Math.PI)) / 2;
    float outputScale = (float) (.66D + outputFactor * .18D + hoverFactor * .05D);
    float lift = hoverFactor * .02F;

    ms.push();
    ms.translate(-.015D, .03D + lift, -.02D);
    ms.scale(outputScale, 1F, outputScale);
    ms.rotate(Vector3f.XP.rotationDegrees(90));
    ms.rotate(Vector3f.YP.rotationDegrees(180));
    RenderUtil.renderFlatItem(ms, currentOutput);
    ms.pop();

    renderCraftButtonOutputCount(ms, currentOutput, outputScale, lift);
  }

  private void renderCraftButtonOutputCount(MatrixStack ms, ItemStack stack, float outputScale, float lift) {
    if (stack.getCount() <= 1) {
      return;
    }

    String text = TextHelper.formatBigNumber(stack.getCount(), true);

    ms.push();
    ms.translate(-.015D, .03D + lift, -.02D);
    ms.scale(outputScale, 1F, outputScale);
    ms.rotate(Vector3f.XP.rotationDegrees(90));
    float ts = 1F / 24F;
    ms.scale(ts, ts, ts);
    ms.translate(-10D, -4D, -0.08D);
    AnimatedItemStack.renderAmountText(ms, text, 0xFFFFFFFF, TEXT_BUFFERS);

    ms.pop();
  }

  public void renderSlotBackground(MatrixStack ms, float r, float g, float b, float alpha) {
    renderPanel(ms, 1F, 1F, SLOT_CORNER_RADIUS, SLOT_FADE_WIDTH, SLOT_SEGMENTS,
        0F, 0F, 0F, 0F, r, g, b, alpha);
  }

  private float[] getPlateColor(float hoverFactor) {
    float phase = (float) (RenderTick.total * .01D);
    float[] primary = getStyledColor(false, phase);
    float[] accent = getStyledColor(true, phase + .45D);
    return blendColors(primary, accent, .14F + hoverFactor * .3F);
  }

  private float[] getGlowColor() {
    return getStyledColor(true, RenderTick.total * .01D + .85D);
  }

  private float[] getStyledColor(boolean accentColor, double phase) {
    HaloInterfaceStyle style = ItemRequestingHalo.getInterfaceStyle(haloStack);
    float[] color = accentColor ? HaloStylePalette.accent(style, phase) : HaloStylePalette.primary(style, phase);
    if (style.isShaderStyle()) {
      return HaloStylePalette.tint(color, ItemRequestingHalo.getHaloTintColor(haloStack), .72F, .05F);
    }
    return color;
  }

  private static float[] blendColors(float[] first, float[] second, float amount) {
    float inverse = 1F - amount;
    return new float[] {
        first[0] * inverse + second[0] * amount,
        first[1] * inverse + second[1] * amount,
        first[2] * inverse + second[2] * amount
    };
  }

  private void renderPanel(MatrixStack ms, float halfWidth, float halfHeight, float cornerRadius, float fadeWidth, int segments,
                           float waveAmplitude, float waveFrequency, float waveSpeed, float waveOffset,
                           float r, float g, float b, float alpha) {
    MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
    VertexConsumer builder = buffers.getBuffer(ModRenderTypes.craftingSlotBg);
    Matrix4f matrix = ms.getLast().getMatrix();
    float stepX = halfWidth * 2F / segments;
    float stepZ = halfHeight * 2F / segments;

    for (int xIndex = 0; xIndex < segments; xIndex++) {
      float x0 = -halfWidth + xIndex * stepX;
      float x1 = x0 + stepX;
      for (int zIndex = 0; zIndex < segments; zIndex++) {
        float z0 = -halfHeight + zIndex * stepZ;
        float z1 = z0 + stepZ;

        float px00 = panelX(x0, z0, halfWidth, halfHeight, fadeWidth, waveAmplitude, waveFrequency, waveSpeed, waveOffset);
        float pz00 = panelZ(x0, z0, halfWidth, halfHeight, fadeWidth, waveAmplitude, waveFrequency, waveSpeed, waveOffset);
        float a00 = alpha * panelAlpha(px00, pz00, halfWidth, halfHeight, cornerRadius, fadeWidth);

        float px01 = panelX(x0, z1, halfWidth, halfHeight, fadeWidth, waveAmplitude, waveFrequency, waveSpeed, waveOffset);
        float pz01 = panelZ(x0, z1, halfWidth, halfHeight, fadeWidth, waveAmplitude, waveFrequency, waveSpeed, waveOffset);
        float a01 = alpha * panelAlpha(px01, pz01, halfWidth, halfHeight, cornerRadius, fadeWidth);

        float px11 = panelX(x1, z1, halfWidth, halfHeight, fadeWidth, waveAmplitude, waveFrequency, waveSpeed, waveOffset);
        float pz11 = panelZ(x1, z1, halfWidth, halfHeight, fadeWidth, waveAmplitude, waveFrequency, waveSpeed, waveOffset);
        float a11 = alpha * panelAlpha(px11, pz11, halfWidth, halfHeight, cornerRadius, fadeWidth);

        float px10 = panelX(x1, z0, halfWidth, halfHeight, fadeWidth, waveAmplitude, waveFrequency, waveSpeed, waveOffset);
        float pz10 = panelZ(x1, z0, halfWidth, halfHeight, fadeWidth, waveAmplitude, waveFrequency, waveSpeed, waveOffset);
        float a10 = alpha * panelAlpha(px10, pz10, halfWidth, halfHeight, cornerRadius, fadeWidth);

        if (a00 <= .001F && a01 <= .001F && a11 <= .001F && a10 <= .001F) {
          continue;
        }

        builder.vertex(matrix, px11, 0, pz11).color(r, g, b, a11).endVertex();
        builder.vertex(matrix, px10, 0, pz10).color(r, g, b, a10).endVertex();
        builder.vertex(matrix, px00, 0, pz00).color(r, g, b, a00).endVertex();
        builder.vertex(matrix, px01, 0, pz01).color(r, g, b, a01).endVertex();
      }
    }

    buffers.endBatch(ModRenderTypes.craftingSlotBg);
  }

  private static float panelX(float x, float z, float halfWidth, float halfHeight, float fadeWidth,
                              float waveAmplitude, float waveFrequency, float waveSpeed, float waveOffset) {
    if (waveAmplitude <= 0F) {
      return x;
    }
    float edgeDistance = halfWidth - Math.abs(x);
    float edgeInfluence = edgeInfluence(edgeDistance, fadeWidth);
    if (edgeInfluence <= 0F) {
      return x;
    }
    double phase = z / Math.max(.001F, halfHeight) * waveFrequency + RenderTick.total * waveSpeed + waveOffset;
    return x + Math.signum(x) * (float) Math.sin(phase) * waveAmplitude * edgeInfluence;
  }

  private static float panelZ(float x, float z, float halfWidth, float halfHeight, float fadeWidth,
                              float waveAmplitude, float waveFrequency, float waveSpeed, float waveOffset) {
    if (waveAmplitude <= 0F) {
      return z;
    }
    float edgeDistance = halfHeight - Math.abs(z);
    float edgeInfluence = edgeInfluence(edgeDistance, fadeWidth);
    if (edgeInfluence <= 0F) {
      return z;
    }
    double phase = x / Math.max(.001F, halfWidth) * waveFrequency + RenderTick.total * waveSpeed + waveOffset + 1.3D;
    return z + Math.signum(z) * (float) Math.cos(phase) * waveAmplitude * .8F * edgeInfluence;
  }

  private static float panelAlpha(float x, float z, float halfWidth, float halfHeight, float cornerRadius, float fadeWidth) {
    float distance = roundedRectDistance(x, z, halfWidth, halfHeight, cornerRadius);
    return MathHelper.clamp(-distance / Math.max(.001F, fadeWidth), 0F, 1F);
  }

  private static float roundedRectDistance(float x, float z, float halfWidth, float halfHeight, float cornerRadius) {
    float innerHalfWidth = Math.max(.001F, halfWidth - cornerRadius);
    float innerHalfHeight = Math.max(.001F, halfHeight - cornerRadius);
    float qx = Math.abs(x) - innerHalfWidth;
    float qz = Math.abs(z) - innerHalfHeight;
    float outsideX = Math.max(qx, 0F);
    float outsideZ = Math.max(qz, 0F);
    float outsideDistance = MathHelper.sqrt(outsideX * outsideX + outsideZ * outsideZ);
    float insideDistance = Math.min(Math.max(qx, qz), 0F);
    return outsideDistance + insideDistance - cornerRadius;
  }

  private static float edgeInfluence(float edgeDistance, float fadeWidth) {
    return smoothstep(0F, fadeWidth * 1.35F, fadeWidth * 1.35F - edgeDistance);
  }

  private static float smoothstep(float edge0, float edge1, float value) {
    float amount = MathHelper.clamp((value - edge0) / Math.max(.001F, edge1 - edge0), 0F, 1F);
    return amount * amount * (3F - 2F * amount);
  }

  public Vector3 getInteractionPlanePoint() {
    return new Vector3(0, 0, pos).rotate(-rotation, Y_AXIS);
  }

  public Vector3 getInteractionPlaneNormal() {
    return new Vector3(0, 1, 0)
        .rotate(-GRID_TILT_RADIANS, X_AXIS)
        .rotate(-rotation, Y_AXIS);
  }

  public void tick(@Nullable Vector3 worldPos) {
    rotationSpd = MathUtil.smoothMovingSpeed(rotation, targetRotation, rotationSpd, .1, .8, .01);

    pointingLocalPos = worldPos == null ? NOT_POINTING : toLocalPos(worldPos);
  }

  public boolean tryOpenJei() {
    if (isPointingAtGrid()) {
      mc.setScreen(new DummyTransferringGui());
      return true;
    }
    return false;
  }

  private void saveShadowItemToNBT(int slot, ItemStack shadow) {
    ItemRequestingHalo.setShadowStackInCraftingSlot(haloStack, slot, shadow);
  }

  /**
   * Remember to call updateRecipe() after changing all the slots.
   * This is to prevent updating the recipe when changing every item
   */
  public boolean tryPlaceShadowItem(int slot, ItemStack stack) {
    if (slots[slot].setShadowStack(stack)) {
      saveShadowItemToNBT(slot, stack);
      ModPacketHandler.sendToServer(new CPacketSetHaloCraftingShadowSlot(haloItemSlot, slot, stack));
      return true;
    }
    return false;
  }

  public boolean handleSlotInteraction(boolean isPut, @Nullable Vector3 clickWorldPos, @Nullable HaloPickedItem pickedItem) {
    if (mc.player == null || mc.level == null) return false;
    if (clickWorldPos == null) return false;
    CraftingInterfaceSlot slot = getPointingSlot();
    if (slot == null) return false;

    ItemStack shadowStack = slot.getShadowStack();
    ItemStack realStack = slot.getRealStack();

    if (isPut) {
      if (pickedItem == null) return false;
      ItemStack pickedStack = pickedItem.getStack();
      if (tryPlaceShadowItem(slot.getSlotIndex(), pickedStack)) {
        updateRecipe();
        return true;
      }
    } else {
      if (!realStack.isEmpty()) {
        ModPacketHandler.sendToServer(new CPacketChangeStackInHaloCraftingSlot(haloItemSlot, slot.getSlotIndex(), false, clickWorldPos));
        return true;
      }
      if (!shadowStack.isEmpty()) {
        if (tryPlaceShadowItem(slot.getSlotIndex(), ItemStack.EMPTY)) {
          updateRecipe();
          return true;
        }
      }
    }

    return false;
  }

  public void updateRecipe() {
    if (mc.level == null || mc.player == null) return;

    RecipeManager manager = mc.level.getRecipeManager();
    CraftingContainer craftingInv = new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, -1) {
      @Override
      public ItemStack quickMoveStack(@Nonnull Player player, int slot) {
        return ItemStack.EMPTY;
      }

      @Override
      public boolean stillValid(@Nonnull Player player) {
        return false;
      }
    }, 3, 3);
    for (int i = 0; i < 9; i++) {
      ItemStack stack = slots[i].getShadowStack();
      if (stack.isEmpty()) continue;
      stack = stack.copy();
      stack.setCount(1);
      craftingInv.setItem(i, stack);
    }

    Optional<CraftingRecipe> recipe = manager.getRecipeFor(RecipeType.CRAFTING, craftingInv, mc.level);
    currentRecipe = recipe.orElse(null);

    ItemStack oldOutput = currentOutput.copy();

    if (currentRecipe != null) {
      currentOutput = currentRecipe.assemble(craftingInv, mc.level.registryAccess());
      currentRemainingItems = currentRecipe.getRemainingItems(craftingInv);
    } else {
      currentOutput = ItemStack.EMPTY;
      currentRemainingItems = NonNullList.withSize(9, ItemStack.EMPTY);
    }

    if (!oldOutput.isEmpty() && (!ItemStack.isSameItemSameTags(oldOutput, currentOutput) || oldOutput.getCount() != currentOutput.getCount())) {
      craftingOutputAnimation = 0;
    }
  }

  /**
   * Do the crafting of the current recipe.
   * This will also request missing items from the corporea network.
   * @return if the crafting was successful (this ONLY means the craft request packet was sent to the server, it still might fail on the server side (probably because of lag))
   */
  public boolean doCraft() {
    if (currentRecipe == null) return false;
    ModPacketHandler.sendToServer(new CPacketDoCraft(haloItemSlot, 0));
    return true;
  }

  @Nullable
  public CraftingRecipe getCurrentRecipe() {
    return currentRecipe;
  }

  public ItemStack getCurrentOutput() {
    return currentOutput;
  }

  public ItemStack getRemainingItem(int slot) {
    if (currentRemainingItems == null) return null;
    return currentRemainingItems.get(slot);
  }

  public boolean isPointingAtInterface() {
    return isPointingAtGrid() || (showCraftButton && isPointingAtCraftButton());
  }

  public boolean isPointingAtCraftButton() {
    return Math.abs(pointingLocalPos.x - CRAFT_BUTTON_CENTER_X) < CRAFT_BUTTON_HALF_WIDTH
        && Math.abs(pointingLocalPos.y) < CRAFT_BUTTON_HALF_HEIGHT;
  }

  @Nullable
  public CraftingInterfaceSlot getPointingSlot() {
    for (CraftingInterfaceSlot slot : slots) {
      if (slot.isPointIn(pointingLocalPos))
        return slot;
    }
    return null;
  }

  public void setTargetRotation(double rotation) {
    this.targetRotation = rotation;
  }

  public void setPos(double pos) {
    this.pos = pos;
  }

  public void setSize(double size) {
    this.size = size;
  }

  private Vec2d toLocalPos(Vector3 worldPos) {
    Vector3 planePoint = getInteractionPlanePoint();
    Vector3 xAxis = new Vector3(1, 0, 0).rotate(-rotation, Y_AXIS);
    Vector3 zAxis = new Vector3(0, 0, 1)
        .rotate(-GRID_TILT_RADIANS, X_AXIS)
        .rotate(-rotation, Y_AXIS);
    Vector3 relativePos = worldPos.subtract(planePoint);
    return new Vec2d(relativePos.dotProduct(xAxis) / size, relativePos.dotProduct(zAxis) / size);
  }

  private boolean isPointingAtGrid() {
    return Math.abs(pointingLocalPos.x) < 1 && Math.abs(pointingLocalPos.y) < 1;
  }
}
