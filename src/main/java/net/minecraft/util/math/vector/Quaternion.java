package net.minecraft.util.math.vector;

public class Quaternion extends org.joml.Quaternionf {
  public Quaternion() {
  }

  public Quaternion(org.joml.Quaternionfc quaternion) {
    super(quaternion);
  }

  public Quaternion(Vector3f axis, float angle, boolean degrees) {
    if (degrees) {
      fromAxisAngleDeg(axis.x, axis.y, axis.z, angle);
    } else {
      fromAxisAngleRad(axis.x, axis.y, axis.z, angle);
    }
  }

  public Quaternion(float x, float y, float z, boolean degrees) {
    if (degrees) {
      rotationXYZ((float) Math.toRadians(x), (float) Math.toRadians(y), (float) Math.toRadians(z));
    } else {
      rotationXYZ(x, y, z);
    }
  }

  public void multiply(Quaternion quaternion) {
    mul(quaternion);
  }

  public void multiply(org.joml.Quaternionfc quaternion) {
    mul(quaternion);
  }
}