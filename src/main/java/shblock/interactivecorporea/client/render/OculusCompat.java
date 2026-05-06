package shblock.interactivecorporea.client.render;

import shblock.interactivecorporea.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class OculusCompat {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final long STATUS_LOG_INTERVAL_MS = 2000L;

  private static boolean initialized;
  private static boolean unavailable;
  private static boolean loggedInitializationSuccess;
  private static boolean loggedMissingPipeline;
  private static boolean loggedMissingListener;
  private static boolean loggedLegacyApiUnavailable;
  private static long nextStatusLogAt;
  private static String resolvedRootPackage;
  private static String resolvedClassLoader;
  private static boolean legacyOverrideApiAvailable;
  private static Method getPipelineManager;
  private static Method getPipeline;
  private static Method syncProgram;
  private static Method getRenderTargetStateListener;
  private static Method beginPostChain;
  private static Method endPostChain;
  private static Method unlockDepthColor;

  private OculusCompat() {
  }

  private static void debugInfo(String message, Object... args) {
    if (ModConfig.isShaderDebugEnabled()) {
      LOGGER.info(message, args);
    }
  }

  private static void debugWarn(String message, Object... args) {
    if (ModConfig.isShaderDebugEnabled()) {
      LOGGER.warn(message, args);
    }
  }

  private static void debugWarn(String message, Throwable throwable) {
    if (ModConfig.isShaderDebugEnabled()) {
      LOGGER.warn(message, throwable);
    }
  }

  public static void withoutGbufferOverride(Runnable renderer) {
    if (!initialize()) {
      renderer.run();
      return;
    }

    Object pipeline = getPipeline();
    Object listener = pipeline == null ? null : getRenderTargetStateListener(pipeline);
    if (legacyOverrideApiAvailable && pipeline != null && listener != null) {
      try {
        beginPostChain.invoke(listener);
        syncProgram.invoke(pipeline);
        logCompatActive(pipeline, listener);
      } catch (ReflectiveOperationException | RuntimeException e) {
        debugWarn("[HaloShaderDebug] Oculus compat beginPostChain/syncProgram failed; rendering without override.", e);
        unavailable = true;
        renderer.run();
        return;
      }

      try {
        renderer.run();
      } finally {
        try {
          endPostChain.invoke(listener);
          syncProgram.invoke(pipeline);
        } catch (ReflectiveOperationException | RuntimeException e) {
          debugWarn("[HaloShaderDebug] Oculus compat endPostChain/syncProgram failed after halo shader draw.", e);
          unavailable = true;
        }
      }
      return;
    }

    logLegacyApiUnavailable();
    if (pipeline == null) {
      logMissingPipeline();
    } else if (listener == null) {
      logMissingListener(pipeline);
    }
    renderer.run();
  }

  public static void unlockDepthColor() {
    if (!initialize() || unlockDepthColor == null) {
      return;
    }

    try {
      unlockDepthColor.invoke(null);
    } catch (ReflectiveOperationException | RuntimeException e) {
      debugWarn("[HaloShaderDebug] Failed to unlock Oculus depth/color state for halo shader rendering.", e);
    }
  }

  private static Object getPipeline() {
    if (!initialize() || getPipelineManager == null || getPipeline == null) {
      return null;
    }

    try {
      Object pipelineManager = getPipelineManager.invoke(null);
      Optional<?> pipeline = (Optional<?>) getPipeline.invoke(pipelineManager);
      return pipeline.orElse(null);
    } catch (ReflectiveOperationException | RuntimeException e) {
      debugWarn("[HaloShaderDebug] Failed to fetch Oculus pipeline.", e);
      unavailable = true;
      return null;
    }
  }

  private static Object getRenderTargetStateListener(Object pipeline) {
    if (pipeline == null || !initialize()) {
      return null;
    }

    try {
      return getRenderTargetStateListener.invoke(pipeline);
    } catch (ReflectiveOperationException | RuntimeException e) {
      debugWarn("[HaloShaderDebug] Failed to fetch Oculus render target state listener.", e);
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
      ResolvedOculusClasses resolved = resolveOculusClasses();
      Class<?> iris = resolved.iris;
      Class<?> pipelineManager = resolved.pipelineManager;
      Class<?> pipeline = resolved.pipeline;
      Class<?> renderTargetStateListener = resolved.renderTargetStateListener;
      Class<?> depthColorStorage = resolved.depthColorStorage;
      resolvedRootPackage = resolved.rootPackage;
      resolvedClassLoader = resolved.classLoaderDescription;

      getPipelineManager = iris.getMethod("getPipelineManager");
      getPipeline = pipelineManager.getMethod("getPipeline");
      getRenderTargetStateListener = pipeline.getMethod("getRenderTargetStateListener");
      unlockDepthColor = depthColorStorage.getMethod("unlockDepthColor");

      syncProgram = findMethod(pipeline, "syncProgram");
      beginPostChain = findMethod(renderTargetStateListener, "beginPostChain");
      endPostChain = findMethod(renderTargetStateListener, "endPostChain");
      legacyOverrideApiAvailable = syncProgram != null && beginPostChain != null && endPostChain != null;

      if (!loggedInitializationSuccess) {
        debugInfo("[HaloShaderDebug] Oculus compat initialized successfully for halo shader rendering. rootPackage={}, classLoader={}, legacyOverrideApiAvailable={}", resolvedRootPackage, resolvedClassLoader, legacyOverrideApiAvailable);
        loggedInitializationSuccess = true;
      }
    } catch (ClassNotFoundException | NoSuchMethodException | LinkageError e) {
      debugWarn("[HaloShaderDebug] Oculus compat unavailable for halo shader rendering.", e);
      unavailable = true;
    }

    return !unavailable;
  }

  private static ResolvedOculusClasses resolveOculusClasses() throws ClassNotFoundException {
    ClassNotFoundException failure = null;
    for (String rootPackage : new String[] {"net.coderbot.iris", "net.irisshaders.iris"}) {
      try {
        Class<?> iris = loadClass(rootPackage + ".Iris");
        Class<?> pipelineManager = loadClass(rootPackage + ".pipeline.PipelineManager");
        Class<?> pipeline = loadClass(rootPackage + ".pipeline.WorldRenderingPipeline");
        Class<?> renderTargetStateListener = loadFirstPresentClass(
            rootPackage + ".gbuffer_overrides.state.RenderTargetStateListener",
            rootPackage + ".targets.RenderTargetStateListener"
        );
        Class<?> depthColorStorage = loadClass(rootPackage + ".gl.blending.DepthColorStorage");
        return new ResolvedOculusClasses(rootPackage, iris, pipelineManager, pipeline, renderTargetStateListener, depthColorStorage, describeClassLoader(iris.getClassLoader()));
      } catch (ClassNotFoundException e) {
        if (failure == null) {
          failure = e;
        }
      }
    }

    if (failure != null) {
      throw failure;
    }
    throw new ClassNotFoundException("Unable to resolve Oculus/Iris classes.");
  }

  private static Class<?> loadClass(String className) throws ClassNotFoundException {
    ClassNotFoundException failure = null;
    for (ClassLoader classLoader : getCandidateClassLoaders()) {
      if (classLoader == null) {
        continue;
      }

      try {
        return Class.forName(className, false, classLoader);
      } catch (ClassNotFoundException e) {
        if (failure == null) {
          failure = e;
        }
      }
    }

    if (failure != null) {
      throw failure;
    }
    throw new ClassNotFoundException(className);
  }

  private static Class<?> loadFirstPresentClass(String... classNames) throws ClassNotFoundException {
    ClassNotFoundException failure = null;
    for (String className : classNames) {
      try {
        return loadClass(className);
      } catch (ClassNotFoundException e) {
        if (failure == null) {
          failure = e;
        }
      }
    }

    if (failure != null) {
      throw failure;
    }
    throw new ClassNotFoundException("No candidate class could be resolved.");
  }

  private static Method findMethod(Class<?> owner, String name, Class<?>... parameterTypes) {
    try {
      return owner.getMethod(name, parameterTypes);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private static Set<ClassLoader> getCandidateClassLoaders() {
    Set<ClassLoader> classLoaders = new LinkedHashSet<>();
    classLoaders.add(Thread.currentThread().getContextClassLoader());
    classLoaders.add(Minecraft.class.getClassLoader());
    classLoaders.add(OculusCompat.class.getClassLoader());
    classLoaders.add(ClassLoader.getSystemClassLoader());
    return classLoaders;
  }

  private static String describeClassLoader(ClassLoader classLoader) {
    return classLoader == null ? "bootstrap" : classLoader.getClass().getName();
  }

  private static void logMissingPipeline() {
    if (!loggedMissingPipeline) {
      debugInfo("[HaloShaderDebug] No active Oculus pipeline found; halo shader draw will run without Oculus compat override.");
      loggedMissingPipeline = true;
    }
  }

  private static void logMissingListener(Object pipeline) {
    if (!loggedMissingListener) {
      debugWarn("[HaloShaderDebug] Oculus pipeline {} had no render target state listener; halo shader draw will run without override.", pipeline.getClass().getName());
      loggedMissingListener = true;
    }
  }

  private static void logCompatActive(Object pipeline, Object listener) {
    long now = System.currentTimeMillis();
    if (now < nextStatusLogAt) {
      return;
    }
    nextStatusLogAt = now + STATUS_LOG_INTERVAL_MS;
    debugInfo("[HaloShaderDebug] Oculus compat active. pipeline={}, listener={}", pipeline.getClass().getName(), listener.getClass().getName());
  }

  private static void logLegacyApiUnavailable() {
    if (!loggedLegacyApiUnavailable && initialized && !unavailable && !legacyOverrideApiAvailable) {
      debugInfo("[HaloShaderDebug] Oculus legacy pipeline override API is not present in this runtime; halo rendering will rely on ShaderInstance depth/color unlock only.");
      loggedLegacyApiUnavailable = true;
    }
  }

  private record ResolvedOculusClasses(
      String rootPackage,
      Class<?> iris,
      Class<?> pipelineManager,
      Class<?> pipeline,
      Class<?> renderTargetStateListener,
      Class<?> depthColorStorage,
      String classLoaderDescription
  ) {
  }
}