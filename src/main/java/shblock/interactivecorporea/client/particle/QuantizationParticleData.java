package shblock.interactivecorporea.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import vazkii.botania.common.core.helper.Vector3;

import java.util.Locale;

public class QuantizationParticleData implements ParticleOptions {
  public final Vector3 dest;
  public final int time;
  public final ItemStack stack;
  public final boolean quantize;

  public QuantizationParticleData(Vector3 dest, int time, ItemStack stack, boolean quantize) {
    this.dest = dest;
    this.time = time;
    this.stack = stack;
    this.quantize = quantize;
  }

  @Override
  public ParticleType<?> getType() {
    return QuantizationParticleType.INSTANCE;
  }

  @Override
  public void writeToNetwork(FriendlyByteBuf buffer) {
    buffer.writeDouble(dest.x);
    buffer.writeDouble(dest.y);
    buffer.writeDouble(dest.z);
    buffer.writeInt(time);
    buffer.writeItem(stack);
    buffer.writeBoolean(quantize);
  }

  @Override
  public String writeToString() {
    return String.format(Locale.ROOT, "%s %.4f %.4f %.4f %d %s %s",
        BuiltInRegistries.PARTICLE_TYPE.getKey(getType()),
        dest.x, dest.y, dest.z,
        time,
        BuiltInRegistries.ITEM.getKey(stack.getItem()),
        quantize);
  }

  public static final ParticleOptions.Deserializer<QuantizationParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
    @Override
    public QuantizationParticleData fromCommand(ParticleType<QuantizationParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
      reader.expect(' ');
      double dx = reader.readDouble();
      reader.expect(' ');
      double dy = reader.readDouble();
      reader.expect(' ');
      double dz = reader.readDouble();
      reader.expect(' ');
      int time = reader.readInt();
      reader.expect(' ');
      ItemStack itemstack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(reader.readString())));
      reader.expect(' ');
      boolean quantize = reader.readBoolean();

      return new QuantizationParticleData(new Vector3(dx, dy, dz), time, itemstack, quantize);
    }

    @Override
    public QuantizationParticleData fromNetwork(ParticleType<QuantizationParticleData> particleTypeIn, FriendlyByteBuf buffer) {
      return new QuantizationParticleData(
          new Vector3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
          buffer.readInt(),
          buffer.readItem(),
          buffer.readBoolean()
      );
    }
  };
}
