package shblock.interactivecorporea.common.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;

public class WorldHelper {
  @Nullable
  public static Level getWorldFromName(@Nullable ResourceKey<Level> key) {
    if (key == null) return null;
    return DistExecutor.unsafeRunForDist(
        () -> () -> ClientSidedCode.getWorldFromName(key),
        () -> () -> ServerSidedCode.getWorldFromName(key)
    );
  }
}
