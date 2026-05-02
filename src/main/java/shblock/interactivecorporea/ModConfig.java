package shblock.interactivecorporea;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class ModConfig {
  public static class Client {
    public final ForgeConfigSpec.BooleanValue itemRequestingHaloAnimation;
    public final ForgeConfigSpec.BooleanValue enableHaloShaders;

    public Client(ForgeConfigSpec.Builder builder) {
      builder.push("Render");
      itemRequestingHaloAnimation = builder
          .comment("Enable the animation of requesting halo item")
          .define("itemRequestingHaloAnimation", true);
      enableHaloShaders = builder
          .comment("Enable animated GLSL shader backgrounds on the Requesting Halo. Disabling this also hides other players' shader backgrounds.")
          .define("enableHaloShaders", true);
      builder.pop();
    }
  }

  public static final Client CLIENT;
  public static final ForgeConfigSpec CLIENT_SPEC;
  static {
    final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
    CLIENT_SPEC = specPair.getRight();
    CLIENT = specPair.getLeft();
  }

  public static class Common {
    public final ForgeConfigSpec.IntValue requestingHaloStaticConsumption;
    public final ForgeConfigSpec.IntValue requestingHaloUpdateConsumption;
    public final ForgeConfigSpec.IntValue quantizationExtractConsumption;
    public final ForgeConfigSpec.IntValue quantizationInsertConsumption;
    public final ForgeConfigSpec.IntValue quantizationDeviceManaCapacity;

    public final ForgeConfigSpec.IntValue quantizationAnimationSpeed;

    public Common(ForgeConfigSpec.Builder builder) {
      builder.push("Mana");
      requestingHaloStaticConsumption = builder
          .comment("Mana consumption per tick while the halo interface is open")
          .defineInRange("requestingHaloStaticConsumption", 1, 0, Integer.MAX_VALUE);
      requestingHaloUpdateConsumption = builder
          .comment("Mana consumption when the displayed item list is updated")
          .defineInRange("requestingHaloUpdateConsumption", 10, 0, Integer.MAX_VALUE);
      quantizationExtractConsumption = builder
          .comment("Mana consumption PER ITEM to extract items from the network")
          .defineInRange("quantizationExtractConsumption", 20, 0, Integer.MAX_VALUE);
      quantizationInsertConsumption = builder
          .comment("Mana consumption PER ITEM to insert items into the network")
          .defineInRange("quantizationInsertConsumption", 40, 0, Integer.MAX_VALUE);
      quantizationDeviceManaCapacity = builder
          .comment("The mana capacity of the Quantization Device (recommended to be larger than the per-item quantization costs * 256)")
          .defineInRange("quantizationDeviceManaCapacity", 100000, 1, Integer.MAX_VALUE);
      builder.pop();

      builder.push("Animations");
      quantizationAnimationSpeed = builder
          .comment("The animation speed of the quantization of items (that's the ticks of one visual stage, and the full animation time is roughly 2 * <this value>)")
          .defineInRange("quantizationAnimationSpeed", 6, 1, 100);
      builder.pop();
    }
  }

  public static final Common COMMON;
  public static final ForgeConfigSpec COMMON_SPEC;
  static {
    final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
    COMMON_SPEC = specPair.getRight();
    COMMON = specPair.getLeft();
  }
}
