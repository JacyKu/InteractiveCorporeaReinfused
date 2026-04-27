package shblock.interactivecorporea.client.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import shblock.interactivecorporea.client.render.shader.SimpleShaderProgram;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.client.util.RenderTick;

import java.util.Objects;

import static org.lwjgl.opengl.GL44.*;

public class DummyTransferringGui extends AbstractContainerScreen<DummyTransferringContainer> {
  private static final SimpleShaderProgram shader = new SimpleShaderProgram("jei_bg", Uniforms::init);

  private static class Uniforms {
    private static int TIME;
    private static int EDGE;
    private static int GUI_SCALE;

    private static void init(SimpleShaderProgram shader) {
      TIME = shader.getUniformLocation("time");
      EDGE = shader.getUniformLocation("edge");
      GUI_SCALE = shader.getUniformLocation("guiScale");
    }
  }

  private double openCloseProgress = 0;
  private boolean closing = false;

  public DummyTransferringGui() {
    super(new DummyTransferringContainer(), Objects.requireNonNull(Minecraft.getInstance().player).getInventory(), Component.literal(""));
  }

  private void updateGuiSize() {
    width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
    height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
    double factor = 1 - (Math.cos(openCloseProgress * Math.PI) + 1) / 2;
    leftPos = (int) (width / 3 * factor);
    topPos = 0;
    imageWidth = (int) (width - (width / 3 * factor) - leftPos);
    imageHeight = height;
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

    updateGuiSize();

    shader.use();
    glUniform1f(Uniforms.TIME, (float) (RenderTick.total / 20));
    glUniform1f(Uniforms.GUI_SCALE, (float) Minecraft.getInstance().getWindow().getGuiScale());

    glUniform1f(Uniforms.EDGE, leftPos);
    gui.fill(0, 0, leftPos, height, 0);

    glUniform1f(Uniforms.EDGE, leftPos + imageWidth);
    gui.fill(leftPos + imageWidth, 0, width, height, 0);

    shader.release();
  }

  @Override
  protected void renderBg(GuiGraphics gui, float partialTicks, int x, int y) { }
}
