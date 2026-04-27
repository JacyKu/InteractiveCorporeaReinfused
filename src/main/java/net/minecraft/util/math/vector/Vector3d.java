package net.minecraft.util.math.vector;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Vector3d extends Vec3 {
  public Vector3d(double x, double y, double z) {
    super(x, y, z);
  }

  public Vector3d(Vec3 vec) {
    this(vec.x, vec.y, vec.z);
  }

  public static Vector3d fromPitchYaw(float pitch, float yaw) {
    float yawCos = Mth.cos(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
    float yawSin = Mth.sin(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
    float pitchCos = -Mth.cos(-pitch * ((float) Math.PI / 180F));
    float pitchSin = Mth.sin(-pitch * ((float) Math.PI / 180F));
    return new Vector3d(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
  }

  public Vector3d rotateYaw(float yaw) {
    float cos = Mth.cos(yaw);
    float sin = Mth.sin(yaw);
    double rotatedX = x * cos + z * sin;
    double rotatedZ = z * cos - x * sin;
    return new Vector3d(rotatedX, y, rotatedZ);
  }

  public Vector3d rotatePitch(float pitch) {
    float cos = Mth.cos(pitch);
    float sin = Mth.sin(pitch);
    double rotatedY = y * cos + z * sin;
    double rotatedZ = z * cos - y * sin;
    return new Vector3d(x, rotatedY, rotatedZ);
  }

  @Override
  public Vector3d add(double x, double y, double z) {
    return new Vector3d(super.add(x, y, z));
  }

  @Override
  public Vector3d add(Vec3 vec) {
    return new Vector3d(super.add(vec));
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getZ() {
    return z;
  }
}