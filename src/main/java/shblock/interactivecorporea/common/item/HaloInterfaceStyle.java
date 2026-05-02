package shblock.interactivecorporea.common.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import shblock.interactivecorporea.IC;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.lib.BotaniaTags;

import javax.annotation.Nullable;
import java.util.Locale;

public enum HaloInterfaceStyle {
  // ── Legacy petal-triggered styles
  CLASSIC("classic",      BotaniaTags.Items.PETALS_LIGHT_BLUE, null),
  MANA("mana",            BotaniaTags.Items.PETALS_CYAN,        null),
  CORPOREA("corporea",    BotaniaTags.Items.PETALS_PURPLE,      null),
  BOTANIA("botania",      BotaniaTags.Items.PETALS_LIME,        null),

  // ── Mystical-flower shader styles (one per shader)
  CLOUDS("clouds",             null, DyeColor.WHITE),
  SPACE("space",               null, DyeColor.BLUE),
  FALLINGSTARS("fallingstars", null, DyeColor.YELLOW),
  LAVALAMP("lavalamp",         null, DyeColor.RED),
  DEPTHMAP("depthmap",         null, DyeColor.GRAY),
  FOGGYCLOUDS("foggyclouds",   null, DyeColor.LIGHT_BLUE),
  GLASSLIQUID("glassliquid",   null, DyeColor.CYAN),
  METALCLOUDS("metalclouds",   null, DyeColor.LIGHT_GRAY),
  SMOKISH("smokish",           null, DyeColor.PURPLE),
  SPLIT("split",               null, DyeColor.GREEN),
  WAVYFOG("wavyfog",           null, DyeColor.PINK),
  WAVYPATTERN("wavypattern",   null, DyeColor.ORANGE);

  private final String serializedName;
  @Nullable private final TagKey<Item> petalTag;
  @Nullable private final DyeColor    flowerDye;
  public final String translationKey;

  HaloInterfaceStyle(String serializedName, @Nullable TagKey<Item> petalTag, @Nullable DyeColor flowerDye) {
    this.serializedName = serializedName;
    this.petalTag       = petalTag;
    this.flowerDye      = flowerDye;
    this.translationKey = IC.MODID + ".halo_style." + serializedName;
  }

  public String getSerializedName() { return serializedName; }

  /** Returns true for styles that use a GLSL shader (unlocked via Mystical Flower). */
  public boolean isShaderStyle() { return flowerDye != null; }

  @Nullable
  public TagKey<Item> getPetalTag() { return petalTag; }

  @Nullable
  public DyeColor getFlowerDye() { return flowerDye; }

  public static HaloInterfaceStyle fromSerializedName(String name) {
    String normalized = name.toLowerCase(Locale.ROOT);
    for (HaloInterfaceStyle style : values()) {
      if (style.serializedName.equals(normalized)) return style;
    }
    return CLASSIC;
  }

  @Nullable
  public static HaloInterfaceStyle fromFlower(ItemStack stack) {
    for (HaloInterfaceStyle style : values()) {
      if (style.flowerDye != null) {
        Block flower = getMysticalFlowerForDye(style.flowerDye);
        if (flower != null && stack.is(flower.asItem())) return style;
      }
    }
    return null;
  }

  @Nullable
  public static HaloInterfaceStyle fromPetal(ItemStack stack) {
    for (HaloInterfaceStyle style : values()) {
      if (style.petalTag != null && stack.is(style.petalTag)) return style;
    }
    return null;
  }

  @Nullable
  public static DyeColor dyeColorFromPetal(ItemStack stack) {
    for (DyeColor dye : DyeColor.values()) {
      if (stack.is(BotaniaTags.Items.getPetalTag(dye))) return dye;
    }
    return null;
  }

  @Nullable
  private static Block getMysticalFlowerForDye(DyeColor dye) {
    return switch (dye) {
      case WHITE      -> BotaniaBlocks.whiteFlower;
      case ORANGE     -> BotaniaBlocks.orangeFlower;
      case MAGENTA    -> BotaniaBlocks.magentaFlower;
      case LIGHT_BLUE -> BotaniaBlocks.lightBlueFlower;
      case YELLOW     -> BotaniaBlocks.yellowFlower;
      case LIME       -> BotaniaBlocks.limeFlower;
      case PINK       -> BotaniaBlocks.pinkFlower;
      case GRAY       -> BotaniaBlocks.grayFlower;
      case LIGHT_GRAY -> BotaniaBlocks.lightGrayFlower;
      case CYAN       -> BotaniaBlocks.cyanFlower;
      case PURPLE     -> BotaniaBlocks.purpleFlower;
      case BLUE       -> BotaniaBlocks.blueFlower;
      case BROWN      -> BotaniaBlocks.brownFlower;
      case GREEN      -> BotaniaBlocks.greenFlower;
      case RED        -> BotaniaBlocks.redFlower;
      case BLACK      -> BotaniaBlocks.blackFlower;
    };
  }
}