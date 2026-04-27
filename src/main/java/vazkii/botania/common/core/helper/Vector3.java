package vazkii.botania.common.core.helper;

import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import vazkii.botania.common.helper.VecHelper;

public class Vector3 {
  public final double x;
  public final double y;
  public final double z;

  public Vector3(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vector3(Vec3 vec) {
    this(vec.x, vec.y, vec.z);
  }

  public Vector3(Vector3f vec) {
    this(vec.x(), vec.y(), vec.z());
  }

  public Vector3(Vector3 vec) {
    this(vec.x, vec.y, vec.z);
  }

  public Vector3 add(Vector3 vec) {
    return add(vec.x, vec.y, vec.z);
  }

  public Vector3 add(double x, double y, double z) {
    return new Vector3(this.x + x, this.y + y, this.z + z);
  }

  public Vector3 subtract(Vector3 vec) {
    return subtract(vec.x, vec.y, vec.z);
  }

  public Vector3 subtract(double x, double y, double z) {
    return new Vector3(this.x - x, this.y - y, this.z - z);
  }

  public Vector3 multiply(double multiplier) {
    return new Vector3(x * multiplier, y * multiplier, z * multiplier);
  }

  public Vector3 multiply(double x, double y, double z) {
    return new Vector3(this.x * x, this.y * y, this.z * z);
  }

  public Vector3 multiply(Vector3 vec) {
    return multiply(vec.x, vec.y, vec.z);
  }

  public Vector3 normalize() {
    double magnitude = mag();
    if (magnitude == 0) {
      return this;
    }

    return multiply(1D / magnitude);
  }

  public double dotProduct(Vector3 vec) {
    return x * vec.x + y * vec.y + z * vec.z;
  }

  public Vector3 crossProduct(Vector3 vec) {
    return new Vector3(
        y * vec.z - z * vec.y,
        z * vec.x - x * vec.z,
        x * vec.y - y * vec.x
    );
  }

  public Vector3 yCrossProduct() {
    return crossProduct(new Vector3(0, 1, 0));
  }

  public Vector3 perpendicular() {
    Vector3 basis = Math.abs(x) < Math.abs(z) ? new Vector3(1, 0, 0) : new Vector3(0, 0, 1);
    Vector3 cross = crossProduct(basis);
    if (cross.mag() == 0) {
      cross = crossProduct(new Vector3(0, 1, 0));
    }
    return cross;
  }

  public Vector3 project(Vector3 vec) {
    double lengthSq = vec.dotProduct(vec);
    if (lengthSq == 0) {
      return new Vector3(0, 0, 0);
    }

    return vec.multiply(dotProduct(vec) / lengthSq);
  }

  public double mag() {
    return Math.sqrt(x * x + y * y + z * z);
  }

  public Vector3 negate() {
    return multiply(-1);
  }

  public Vector3 abs() {
    return new Vector3(Math.abs(x), Math.abs(y), Math.abs(z));
  }

  public Vector3 rotate(double radians, Vector3 axis) {
    return new Vector3(VecHelper.rotate(toVec3(), radians, axis.toVec3()));
  }

  public Vector3 copy() {
    return new Vector3(this);
  }

  public Vec3 toVec3() {
    return new Vec3(x, y, z);
  }

  @Override
  public String toString() {
    return "Vector3{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}