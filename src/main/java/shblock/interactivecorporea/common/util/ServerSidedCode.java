package shblock.interactivecorporea.common.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;

public class ServerSidedCode {
  @Nullable
  public static Level getWorldFromName(ResourceKey<Level> key) {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    return server == null ? null : server.getLevel(key);
  }
}
