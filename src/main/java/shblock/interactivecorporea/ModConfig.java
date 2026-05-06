package shblock.interactivecorporea;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import shblock.interactivecorporea.common.item.HaloModule;

public final class ModConfig {
  public enum HaloShaderQuality {
    LOW(Math.PI / 180D),
    MEDIUM(Math.PI / 360D),
    HIGH(Math.PI / 720D);

    private final double angleStep;

    HaloShaderQuality(double angleStep) {
      this.angleStep = angleStep;
    }

    public double getAngleStep() {
      return angleStep;
    }
  }

  public static class Client {
    public final ForgeConfigSpec.BooleanValue itemRequestingHaloAnimation;
    public final ForgeConfigSpec.BooleanValue enableHaloShaders;
    public final ForgeConfigSpec.BooleanValue enableShaderDebugLogging;
    public final ForgeConfigSpec.EnumValue<HaloShaderQuality> haloShaderQuality;

    public Client(ForgeConfigSpec.Builder builder) {
      builder.push("Render");
      itemRequestingHaloAnimation = builder
          .comment("Enable the animation of requesting halo item")
          .define("itemRequestingHaloAnimation", true);
      enableHaloShaders = builder
          .comment("Enable animated GLSL shader backgrounds on the Requesting Halo. Disabling this also hides other players' shader backgrounds.")
          .define("enableHaloShaders", true);
      builder.pop();

      builder.push("Shaders");
      enableShaderDebugLogging = builder
          .comment("Enable [HaloShaderDebug] log output for shader compatibility troubleshooting")
          .define("enableShaderDebugLogging", false);
      haloShaderQuality = builder
          .comment("Geometry quality for Requesting Halo shader backgrounds")
          .defineEnum("haloShaderQuality", HaloShaderQuality.MEDIUM);
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

    public final ForgeConfigSpec.BooleanValue enableHudModule;
    public final ForgeConfigSpec.BooleanValue enableReceiveModule;
    public final ForgeConfigSpec.BooleanValue enableSearchModule;
    public final ForgeConfigSpec.BooleanValue enableUpdateModule;
    public final ForgeConfigSpec.BooleanValue enableAnchorModule;
    public final ForgeConfigSpec.BooleanValue enableMagnateModule;
    public final ForgeConfigSpec.BooleanValue enableCraftingModule;
    public final ForgeConfigSpec.BooleanValue enableQuantumInserterModule;
    public final ForgeConfigSpec.BooleanValue enableFarReachModule;
    public final ForgeConfigSpec.BooleanValue enableGreaterMagnateModule;
    public final ForgeConfigSpec.BooleanValue enableBlackHoleModule;

    public final ForgeConfigSpec.IntValue baseRequestingHaloRange;
    public final ForgeConfigSpec.IntValue magnateRangeBonus;
    public final ForgeConfigSpec.IntValue farReachRangeBonus;
    public final ForgeConfigSpec.IntValue greaterMagnateRangeBonus;
    public final ForgeConfigSpec.IntValue blackHoleRangeBonus;

    public final ForgeConfigSpec.BooleanValue enableParticles;
    public final ForgeConfigSpec.DoubleValue particleMultiplier;

    public final ForgeConfigSpec.DoubleValue haloOpenSoundScale;
    public final ForgeConfigSpec.DoubleValue haloCloseSoundScale;
    public final ForgeConfigSpec.DoubleValue haloListUpdateSoundScale;
    public final ForgeConfigSpec.DoubleValue haloSelectSoundScale;
    public final ForgeConfigSpec.DoubleValue haloRequestSoundScale;
    public final ForgeConfigSpec.DoubleValue haloReachEdgeSoundScale;
    public final ForgeConfigSpec.DoubleValue haloOutOfRangeSoundScale;
    public final ForgeConfigSpec.DoubleValue quantumSendSoundScale;
    public final ForgeConfigSpec.DoubleValue quantumReceiveSoundScale;

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

      builder.push("Modules");
      enableHudModule = defineModuleToggle(builder, "enableHudModule", "Enable the HUD Requesting Halo module");
      enableReceiveModule = defineModuleToggle(builder, "enableReceiveModule", "Enable the RECEIVE Requesting Halo module");
      enableSearchModule = defineModuleToggle(builder, "enableSearchModule", "Enable the SEARCH Requesting Halo module");
      enableUpdateModule = defineModuleToggle(builder, "enableUpdateModule", "Enable the UPDATE Requesting Halo module");
      enableAnchorModule = defineModuleToggle(builder, "enableAnchorModule", "Enable the ANCHOR Requesting Halo module");
      enableMagnateModule = defineModuleToggle(builder, "enableMagnateModule", "Enable the MAGNATE Requesting Halo module");
      enableCraftingModule = defineModuleToggle(builder, "enableCraftingModule", "Enable the CRAFTING Requesting Halo module");
      enableQuantumInserterModule = defineModuleToggle(builder, "enableQuantumInserterModule", "Enable the QUANTUM_INSERTER Requesting Halo module");
      enableFarReachModule = defineModuleToggle(builder, "enableFarReachModule", "Enable the FAR_REACH Requesting Halo module");
      enableGreaterMagnateModule = defineModuleToggle(builder, "enableGreaterMagnateModule", "Enable the GREATER_MAGNATE Requesting Halo module");
      enableBlackHoleModule = defineModuleToggle(builder, "enableBlackHoleModule", "Enable the BLACK_HOLE Requesting Halo module");
      builder.pop();

      builder.push("Range");
      baseRequestingHaloRange = builder
          .comment("Base Corporea access range of the Requesting Halo before range upgrades")
          .defineInRange("baseRequestingHaloRange", 10, 0, Integer.MAX_VALUE);
      magnateRangeBonus = builder
          .comment("Range bonus provided by the MAGNATE module when it is enabled")
          .defineInRange("magnateRangeBonus", 10, 0, Integer.MAX_VALUE);
      farReachRangeBonus = builder
          .comment("Range bonus provided by the FAR_REACH module when it is enabled")
          .defineInRange("farReachRangeBonus", 20, 0, Integer.MAX_VALUE);
      greaterMagnateRangeBonus = builder
          .comment("Range bonus provided by the GREATER_MAGNATE module when it is enabled")
          .defineInRange("greaterMagnateRangeBonus", 30, 0, Integer.MAX_VALUE);
      blackHoleRangeBonus = builder
          .comment("Range bonus provided by the BLACK_HOLE module when it is enabled")
          .defineInRange("blackHoleRangeBonus", 50, 0, Integer.MAX_VALUE);
      builder.pop();

      builder.push("Particles");
      enableParticles = builder
          .comment("Enable mod particle effects")
          .define("enableParticles", true);
      particleMultiplier = builder
          .comment("Multiplier applied to mod particle spawn counts and rates")
          .defineInRange("particleMultiplier", 1D, 0D, 10D);
      builder.pop();

      builder.push("Sounds");
      haloOpenSoundScale = defineSoundScale(builder, "haloOpenSoundScale", "Scale applied to halo open sounds");
      haloCloseSoundScale = defineSoundScale(builder, "haloCloseSoundScale", "Scale applied to halo close sounds");
      haloListUpdateSoundScale = defineSoundScale(builder, "haloListUpdateSoundScale", "Scale applied to halo list update sounds");
      haloSelectSoundScale = defineSoundScale(builder, "haloSelectSoundScale", "Scale applied to halo select sounds");
      haloRequestSoundScale = defineSoundScale(builder, "haloRequestSoundScale", "Scale applied to halo request sounds");
      haloReachEdgeSoundScale = defineSoundScale(builder, "haloReachEdgeSoundScale", "Scale applied to halo reach-edge sounds");
      haloOutOfRangeSoundScale = defineSoundScale(builder, "haloOutOfRangeSoundScale", "Scale applied to halo out-of-range sounds");
      quantumSendSoundScale = defineSoundScale(builder, "quantumSendSoundScale", "Scale applied to quantum send sounds");
      quantumReceiveSoundScale = defineSoundScale(builder, "quantumReceiveSoundScale", "Scale applied to quantum receive sounds");
      builder.pop();
    }

    private static ForgeConfigSpec.BooleanValue defineModuleToggle(ForgeConfigSpec.Builder builder, String key, String comment) {
      return builder
          .comment(comment)
          .define(key, true);
    }

    private static ForgeConfigSpec.DoubleValue defineSoundScale(ForgeConfigSpec.Builder builder, String key, String comment) {
      return builder
          .comment(comment)
          .defineInRange(key, 1D, 0D, 10D);
    }

    public boolean isModuleEnabled(HaloModule module) {
      return switch (module) {
        case HUD -> enableHudModule.get();
        case RECEIVE -> enableReceiveModule.get();
        case SEARCH -> enableSearchModule.get();
        case UPDATE -> enableUpdateModule.get();
        case ANCHOR -> enableAnchorModule.get();
        case MAGNATE -> enableMagnateModule.get();
        case CRAFTING -> enableCraftingModule.get();
        case QUANTUM_INSERTER -> enableQuantumInserterModule.get();
        case FAR_REACH -> enableFarReachModule.get();
        case GREATER_MAGNATE -> enableGreaterMagnateModule.get();
        case BLACK_HOLE -> enableBlackHoleModule.get();
      };
    }

    public int getRangeBonus(HaloModule module) {
      return switch (module) {
        case MAGNATE -> magnateRangeBonus.get();
        case FAR_REACH -> farReachRangeBonus.get();
        case GREATER_MAGNATE -> greaterMagnateRangeBonus.get();
        case BLACK_HOLE -> blackHoleRangeBonus.get();
        default -> module.rangeBonus;
      };
    }

    public double getSoundScale(SoundEvent sound) {
      if (sound == ModSounds.haloOpen) return haloOpenSoundScale.get();
      if (sound == ModSounds.haloClose) return haloCloseSoundScale.get();
      if (sound == ModSounds.haloListUpdate) return haloListUpdateSoundScale.get();
      if (sound == ModSounds.haloSelect) return haloSelectSoundScale.get();
      if (sound == ModSounds.haloRequest) return haloRequestSoundScale.get();
      if (sound == ModSounds.haloReachEdge) return haloReachEdgeSoundScale.get();
      if (sound == ModSounds.haloOutOfRange) return haloOutOfRangeSoundScale.get();
      if (sound == ModSounds.quantumSend) return quantumSendSoundScale.get();
      if (sound == ModSounds.quantumReceive) return quantumReceiveSoundScale.get();
      return 1D;
    }
  }

  public static final Common COMMON;
  public static final ForgeConfigSpec COMMON_SPEC;
  static {
    final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
    COMMON_SPEC = specPair.getRight();
    COMMON = specPair.getLeft();
  }

  public static boolean isModuleEnabled(HaloModule module) {
    return COMMON.isModuleEnabled(module);
  }

  public static int getBaseRequestingHaloRange() {
    return COMMON.baseRequestingHaloRange.get();
  }

  public static int getRangeBonus(HaloModule module) {
    return COMMON.getRangeBonus(module);
  }

  public static boolean areParticlesEnabled() {
    return COMMON.enableParticles.get() && COMMON.particleMultiplier.get() > 0D;
  }

  public static double getParticleMultiplier() {
    return areParticlesEnabled() ? COMMON.particleMultiplier.get() : 0D;
  }

  public static double scaleParticleInterval(double baseInterval) {
    double multiplier = getParticleMultiplier();
    if (baseInterval <= 0D || multiplier <= 0D) {
      return Double.POSITIVE_INFINITY;
    }
    return baseInterval / multiplier;
  }

  public static int scaleParticleCount(int baseCount) {
    double multiplier = getParticleMultiplier();
    if (baseCount <= 0 || multiplier <= 0D) {
      return 0;
    }
    return Math.max(0, (int) Math.round(baseCount * multiplier));
  }

  public static float scaleSoundVolume(SoundEvent sound, float volume) {
    return (float) Math.max(0D, volume * COMMON.getSoundScale(sound));
  }

  public static boolean isShaderDebugEnabled() {
    return CLIENT.enableShaderDebugLogging.get();
  }

  public static double getHaloShaderAngleStep() {
    return CLIENT.haloShaderQuality.get().getAngleStep();
  }
}
