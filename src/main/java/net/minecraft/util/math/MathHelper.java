package net.minecraft.util.math;

import net.minecraft.util.Mth;

public final class MathHelper {
  private MathHelper() {
  }

  public static int clamp(int value, int min, int max) {
    return Mth.clamp(value, min, max);
  }

  public static float clamp(float value, float min, float max) {
    return Mth.clamp(value, min, max);
  }

  public static double clamp(double value, double min, double max) {
    return Mth.clamp(value, min, max);
  }

  public static float lerp(float delta, float start, float end) {
    return Mth.lerp(delta, start, end);
  }

  public static double lerp(double delta, double start, double end) {
    return Mth.lerp(delta, start, end);
  }

  public static int hsvToRGB(float hue, float saturation, float value) {
    return Mth.hsvToRgb(hue, saturation, value);
  }

  public static float sqrt(float value) {
    return Mth.sqrt(value);
  }

  public static double sqrt(double value) {
    return Math.sqrt(value);
  }

  public static int floor(double value) {
    return Mth.floor(value);
  }

  public static int ceil(double value) {
    return Mth.ceil(value);
  }
}