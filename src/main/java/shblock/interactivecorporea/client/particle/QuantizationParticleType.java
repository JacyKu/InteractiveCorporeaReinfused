package shblock.interactivecorporea.client.particle;

import com.mojang.serialization.Codec;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleType;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import vazkii.botania.common.core.helper.Vector3;

public class QuantizationParticleType extends ParticleType<QuantizationParticleData> {
  public static final Codec<QuantizationParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Codec.DOUBLE.fieldOf("dest_x").forGetter(data -> data.dest.x),
      Codec.DOUBLE.fieldOf("dest_y").forGetter(data -> data.dest.y),
      Codec.DOUBLE.fieldOf("dest_z").forGetter(data -> data.dest.z),
      Codec.INT.fieldOf("time").forGetter(data -> data.time),
      ItemStack.CODEC.fieldOf("stack").forGetter(data -> data.stack),
      Codec.BOOL.fieldOf("quantize").forGetter(data -> data.quantize)
  ).apply(instance, (x, y, z, time, stack, quantize) -> new QuantizationParticleData(new Vector3(x, y, z), time, stack, quantize)));

  public static final QuantizationParticleType INSTANCE = new QuantizationParticleType();

  public QuantizationParticleType() {
    super(false, QuantizationParticleData.DESERIALIZER);
  }

  @Override
  public Codec<QuantizationParticleData> codec() {
    return CODEC;
  }

  public static class Factory implements ParticleProvider<QuantizationParticleData> {
    private final SpriteSet animatedSprite;

    public Factory(SpriteSet animatedSprite) {
      this.animatedSprite = animatedSprite;
    }

    @Override
    public Particle createParticle(QuantizationParticleData data, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      return new QuantizationParticle(world, animatedSprite, x, y, z, data.dest, data.time, data.stack, data.quantize);
    }
  }
}
