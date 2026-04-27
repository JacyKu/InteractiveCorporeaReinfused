package net.minecraft.util.math.vector;

public class Vector3f extends org.joml.Vector3f {
  public static final Vector3f XP = new Vector3f(1F, 0F, 0F);
  public static final Vector3f YP = new Vector3f(0F, 1F, 0F);
  public static final Vector3f YN = new Vector3f(0F, -1F, 0F);
  public static final Vector3f ZP = new Vector3f(0F, 0F, 1F);

  public Vector3f(float x, float y, float z) {
    super(x, y, z);
  }

  public Vector3f(org.joml.Vector3fc vector) {
    super(vector);
  }

  public Quaternion rotation(float radians) {
    return new Quaternion(this, radians, false);
  }

  public Quaternion rotationDegrees(float degrees) {
    return new Quaternion(this, degrees, true);
  }

  public void transform(Quaternion quaternion) {
    rotate(quaternion);
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public float getZ() {
    return z;
  }
}