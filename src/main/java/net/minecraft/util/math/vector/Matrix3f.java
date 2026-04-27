package net.minecraft.util.math.vector;

public class Matrix3f extends org.joml.Matrix3f {
  public Matrix3f() {
  }

  public Matrix3f(org.joml.Matrix3fc matrix) {
    super(matrix);
  }

  public static Matrix3f makeScaleMatrix(float x, float y, float z) {
    Matrix3f matrix = new Matrix3f();
    matrix.scaling(x, y, z);
    return matrix;
  }
}