package shblock.interactivecorporea.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.common.core.helper.Vector3;

public class QuantizationParticle extends TextureSheetParticle {
  private static final Minecraft mc = Minecraft.getInstance();

  private final SpriteSet animatedSprite;
  private final boolean quantize;
  private final float baseScale;

  protected QuantizationParticle(ClientLevel world, SpriteSet animatedSprite, double x, double y, double z, Vector3 dest, int time, ItemStack stack, boolean quantize) {
    super(world, x, y, z, 0, 0, 0);
    this.animatedSprite = animatedSprite;
    this.quantize = quantize;
    this.lifetime = Math.max(1, time);
    this.baseScale = quantize ? 0.2F : 0.12F;
    this.quadSize = baseScale;
    this.gravity = 0F;
    this.hasPhysics = false;
    this.xd = dest.x / this.lifetime;
    this.yd = dest.y / this.lifetime;
    this.zd = dest.z / this.lifetime;
    float[] itemColor = getColor(stack);
    this.rCol = itemColor[0];
    this.gCol = itemColor[1];
    this.bCol = itemColor[2];
    this.alpha = quantize ? 0.95F : 0.8F;
    setSpriteFromAge(animatedSprite);
  }

  @Override
  public ParticleRenderType getRenderType() {
    return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
  }

  @Override
  public float getQuadSize(float partialTicks) {
    float progress = (age + partialTicks) / (float) lifetime;
    float fade = quantize ? (1.0F - progress * 0.35F) : (0.75F + (1.0F - progress) * 0.25F);
    return baseScale * Math.max(0.2F, fade);
  }

  @Override
  public void tick() {
    setSpriteFromAge(animatedSprite);
    xo = x;
    yo = y;
    zo = z;

    if (age++ >= lifetime) {
      remove();
      return;
    }

    move(xd, yd, zd);
    float damping = quantize ? 0.92F : 0.88F;
    xd *= damping;
    yd *= damping;
    zd *= damping;
    alpha = quantize
        ? Math.max(0.25F, 1F - age / (float) lifetime * 0.75F)
        : Math.max(0.1F, 1F - age / (float) lifetime);
  }

  @Override
  public boolean shouldCull() {
    return false;
  }

  private static float[] getColor(ItemStack stack) {
    int color = mc.getItemColors().getColor(stack, 0);
    if (color == -1) {
      color = 0xFFFFFF;
    }
    return new float[] {
        ((color >> 16) & 0xFF) / 255F,
        ((color >> 8) & 0xFF) / 255F,
        (color & 0xFF) / 255F
    };
  }
}
