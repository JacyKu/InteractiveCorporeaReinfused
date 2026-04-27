package shblock.interactivecorporea.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import shblock.interactivecorporea.ModSounds;
import shblock.interactivecorporea.client.particle.QuantizationParticleData;
import shblock.interactivecorporea.common.network.SPacketPlayQuantizationEffect;
import vazkii.botania.common.core.helper.Vector3;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ClientSidedCode {
  @Nullable
  public static Level getWorldFromName(ResourceKey<Level> key) {
    Level world = Minecraft.getInstance().level;
    if (world != null && world.dimension().equals(key)) {
      return world;
    }
    return null;
  }

  public static void handlePacketPlayQuantizationEffect(int type, ItemStack stack, int time, Vector3 pos, Vector3 normal, double scale, Supplier<NetworkEvent.Context> ctx) {
    Minecraft mc = Minecraft.getInstance();
    ctx.get().enqueueWork(() -> {
      if (mc.level == null) return;
      QuantizationParticleData data;
      switch (type) {
        case SPacketPlayQuantizationEffect.QUANTIZATION:
          mc.level.playLocalSound(pos.x, pos.y, pos.z, ModSounds.quantumSend, SoundSource.PLAYERS, .8F, 1F, false);
          for (int i = 0; i < 512; i++) {
            double particleDist = 2 * scale;
            Vector3 dest = new Vector3(
                SPacketPlayQuantizationEffect.RAND.nextDouble() * 2 - 1,
                SPacketPlayQuantizationEffect.RAND.nextDouble() * 2 - 1,
                SPacketPlayQuantizationEffect.RAND.nextDouble() * 2 - 1)
                .normalize()
                .multiply(particleDist);
            data = new QuantizationParticleData(dest, time, stack, true);
            mc.level.addParticle(data, pos.x, pos.y, pos.z, 0, 0, 0);
          }
          break;
        case SPacketPlayQuantizationEffect.CONSTRUCTION:
          mc.level.playLocalSound(pos.x, pos.y, pos.z, ModSounds.quantumReceive, SoundSource.PLAYERS, .8F, 1F, false);
          Vector3 rotBase = normal.perpendicular().normalize().multiply(.5 * scale);
          for (int i = 0; i < 128; i++) {
            Vector3 roted = rotBase.rotate(SPacketPlayQuantizationEffect.RAND.nextDouble() * Math.PI * 2, normal);
            Vector3 p = roted.add(pos);
            data = new QuantizationParticleData(roted.negate(), time, stack, false);
            mc.level.addParticle(data, true, p.x, p.y, p.z, 0, 0, 0);
          }
          break;
        default:
          assert false;
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
