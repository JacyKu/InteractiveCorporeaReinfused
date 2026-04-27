package shblock.interactivecorporea.client.wormhole;

public final class WormholeRenderer {
  private static boolean shaderEnabled;

  private WormholeRenderer() {
  }

  // The old post-process path depended on removed 1.16 framebuffer/shader APIs.
  // The effect is currently dormant in live code, so keep the toggle surface as a no-op shell.
  public static void loadShader() {
  }

  public static void copyDepth() {
    if (!shaderEnabled) {
      return;
    }
  }

  public static void postProcess() {
    if (!shaderEnabled) {
      return;
    }
  }

  public static boolean isShaderEnabled() {
    return shaderEnabled;
  }

  public static void setShaderEnabled(boolean enable) {
    shaderEnabled = enable;
  }
}
