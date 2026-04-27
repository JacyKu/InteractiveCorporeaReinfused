package shblock.interactivecorporea.client.util;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;

import static org.lwjgl.glfw.GLFW.*;

public class KeyboardHelper {
  private static long window() {
    return Minecraft.getInstance().getWindow().getWindow();
  }

  public static boolean hasControlDown() {
    if (Minecraft.ON_OSX) {
      return InputConstants.isKeyDown(window(), 343) || InputConstants.isKeyDown(window(), 347);
    } else {
      return InputConstants.isKeyDown(window(), 341) || InputConstants.isKeyDown(window(), 345);
    }
  }

  public static boolean hasShiftDown() {
    return InputConstants.isKeyDown(window(), GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window(), GLFW_KEY_RIGHT_SHIFT);
  }

  public static boolean hasAltDown() {
    return InputConstants.isKeyDown(window(), 342) || InputConstants.isKeyDown(window(), 346);
  }
}
