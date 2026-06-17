package shblock.interactivecorporea.client.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.client.util.RenderTick;

import java.util.Objects;

public class DummyTransferringGui extends AbstractContainerScreen<DummyTransferringContainer> {
  private double openCloseProgress = 0;
  private boolean closing = false;

  public DummyTransferringGui() {
    super(new DummyTransferringContainer(Objects.requireNonNull(Minecraft.getInstance().player).getInventory()), Minecraft.getInstance().player.getInventory(), Component.literal(""));
    this.imageWidth = 0;
    this.imageHeight = 0;
  }

  @Override
  protected void init() {
    super.init();
    this.leftPos = 0;
    this.topPos = 0;
    this.imageWidth = 2 * this.width / 3;
    this.imageHeight = this.height;
  }

  public void startClose() {
    closing = true;
  }

  @Override
  public boolean shouldCloseOnEsc() {
    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      startClose();
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
    if (menu.shouldClose || RequestingHaloInterfaceHandler.getInterface() == null || RequestingHaloInterfaceHandler.getInterface().isOpenClose()) {
      startClose();
    }

    if (!closing) {
      openCloseProgress += RenderTick.delta / 5;
      if (openCloseProgress > 1) {
        openCloseProgress = 1;
      }
    } else {
      openCloseProgress -= RenderTick.delta / 5;
      if (openCloseProgress < 0) {
        onClose();
        return;
      }
    }

    super.render(gui, mouseX, mouseY, partialTicks);
  }

  @Override
  protected void renderBg(GuiGraphics gui, float partialTicks, int x, int y) { }

  @Override
  protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) { }
}
