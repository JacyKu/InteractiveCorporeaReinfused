package net.minecraft.util.math.vector;

public class Vector4f extends org.joml.Vector4f {
  public Vector4f(float x, float y, float z, float w) {
    super(x, y, z, w);
  }

  public void transform(Matrix4f matrix) {
    mul(matrix);
  }

  public void perspectiveDivide() {
    if (w != 0F) {
      div(w);
    }
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

  public float getW() {
    return w;
  }
}