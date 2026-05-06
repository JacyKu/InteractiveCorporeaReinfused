package shblock.interactivecorporea.client.render;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import toni.sodiumdynamiclights.DynamicLightSource;
import toni.sodiumdynamiclights.SodiumDynamicLights;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;

class AnchorDynamicLightSource implements DynamicLightSource {
  static final int MAX_LUMINANCE = 12;

  double x, y, z;

  private boolean dynamicLightEnabled;
  private int lastLuminance = -1;
  private double prevX = Double.MIN_VALUE;
  private double prevY = Double.MIN_VALUE;
  private double prevZ = Double.MIN_VALUE;
  private LongOpenHashSet trackedLitChunkPos = new LongOpenHashSet();

  @Override public double getDynamicLightX() { return x; }
  @Override public double getDynamicLightY() { return y; }
  @Override public double getDynamicLightZ() { return z; }

  @Override
  public Level getDynamicLightLevel() {
    return Minecraft.getInstance().level;
  }

  @Override public boolean isDynamicLightEnabled() { return dynamicLightEnabled; }
  @Override public void setDynamicLightEnabled(boolean enabled) { this.dynamicLightEnabled = enabled; }
  @Override public void resetDynamicLight() { lastLuminance = 0; }

  @Override
  public int getLuminance() {
    var face = RequestingHaloInterfaceHandler.getInterface();
    if (face == null || !face.isAnchored()) return 0;
    return (int) Math.round(face.getOpenCloseProgress() * MAX_LUMINANCE);
  }

  @Override
  public void dynamicLightTick() {
    SodiumDynamicLights.updateTracking(this);
  }

  @Override
  public boolean shouldUpdateDynamicLight() {
    return true;
  }

  @Override
  public boolean sodiumdynamiclights$updateDynamicLight(LevelRenderer renderer) {
    double dx = x - prevX;
    double dy = y - prevY;
    double dz = z - prevZ;
    int lum = getLuminance();

    if (Math.abs(dx) <= 0.1 && Math.abs(dy) <= 0.1 && Math.abs(dz) <= 0.1
        && lum == lastLuminance) {
      return false;
    }

    prevX = x;
    prevY = y;
    prevZ = z;
    lastLuminance = lum;

    LongOpenHashSet newSet = new LongOpenHashSet();

    if (lum > 0) {
      int sx = (int) Math.floor(x) >> 4;
      int sy = (int) Math.floor(y) >> 4;
      int sz = (int) Math.floor(z) >> 4;
      BlockPos sectionPos = new BlockPos(sx, sy, sz);
      SodiumDynamicLights.scheduleChunkRebuild(renderer, sectionPos);
      SodiumDynamicLights.updateTrackedChunks(sectionPos, trackedLitChunkPos, newSet);
    }

    sodiumdynamiclights$scheduleTrackedChunksRebuild(renderer);
    trackedLitChunkPos = newSet;
    return true;
  }

  @Override
  public void sodiumdynamiclights$scheduleTrackedChunksRebuild(LevelRenderer renderer) {
    LongIterator iter = trackedLitChunkPos.iterator();
    while (iter.hasNext()) {
      SodiumDynamicLights.scheduleChunkRebuild(renderer, iter.nextLong());
    }
  }
}
