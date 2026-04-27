package vazkii.botania.common.core.helper;

import net.minecraft.world.entity.Entity;

public final class MathHelper {
  private MathHelper() {
  }

  public static void setEntityMotionFromVector(Entity entity, Vector3 originalPosVector, float modifier) {
    vazkii.botania.common.helper.MathHelper.setEntityMotionFromVector(entity, originalPosVector.toVec3(), modifier);
  }
}