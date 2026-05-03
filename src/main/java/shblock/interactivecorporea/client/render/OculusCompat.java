package shblock.interactivecorporea.client.render;

import java.lang.reflect.Method;
import java.util.Optional;

public final class OculusCompat {
  private static boolean initialized;
  private static boolean unavailable;
  private static Method getPipelineManager;
  private static Method getPipeline;
  private static Method getRenderTargetStateListener;
  private static Method beginPostChain;
  private static Method endPostChain;

  private OculusCompat() {
  }

  public static void withoutGbufferOverride(Runnable renderer) {
    Object listener = getRenderTargetStateListener();
    if (listener == null) {
      renderer.run();
      return;
    }

    try {
      beginPostChain.invoke(listener);
    } catch (ReflectiveOperationException | RuntimeException e) {
      unavailable = true;
      renderer.run();
      return;
    }

    try {
      renderer.run();
    } finally {
      try {
        endPostChain.invoke(listener);
      } catch (ReflectiveOperationException | RuntimeException e) {
        unavailable = true;
      }
    }
  }

  private static Object getRenderTargetStateListener() {
    if (!initialize()) {
      return null;
    }

    try {
      Object pipelineManager = getPipelineManager.invoke(null);
      Optional<?> pipeline = (Optional<?>) getPipeline.invoke(pipelineManager);
      if (pipeline.isEmpty()) {
        return null;
      }
      return getRenderTargetStateListener.invoke(pipeline.get());
    } catch (ReflectiveOperationException | RuntimeException e) {
      unavailable = true;
      return null;
    }
  }

  private static boolean initialize() {
    if (initialized) {
      return !unavailable;
    }
    initialized = true;

    try {
      Class<?> iris = Class.forName("net.coderbot.iris.Iris");
      Class<?> pipelineManager = Class.forName("net.coderbot.iris.pipeline.PipelineManager");
      Class<?> pipeline = Class.forName("net.coderbot.iris.pipeline.WorldRenderingPipeline");
      Class<?> renderTargetStateListener = Class.forName("net.coderbot.iris.gbuffer_overrides.state.RenderTargetStateListener");

      getPipelineManager = iris.getMethod("getPipelineManager");
      getPipeline = pipelineManager.getMethod("getPipeline");
      getRenderTargetStateListener = pipeline.getMethod("getRenderTargetStateListener");
      beginPostChain = renderTargetStateListener.getMethod("beginPostChain");
      endPostChain = renderTargetStateListener.getMethod("endPostChain");
    } catch (ClassNotFoundException | NoSuchMethodException | LinkageError e) {
      unavailable = true;
    }

    return !unavailable;
  }
}