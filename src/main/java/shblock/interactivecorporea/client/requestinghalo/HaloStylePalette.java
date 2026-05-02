package shblock.interactivecorporea.client.requestinghalo;

import shblock.interactivecorporea.common.item.HaloInterfaceStyle;
import shblock.interactivecorporea.common.util.MathUtil;

final class HaloStylePalette {
  private HaloStylePalette() {
  }

  static float[] primary(HaloInterfaceStyle style, double phase) {
    return switch (style) {
      case MANA -> color(.528F + wave(phase) * .018F, .48F, 1F);
      case CORPOREA -> color(.77F + wave(phase + .35D) * .025F, .62F, .96F);
      case BOTANIA -> blend(color(.528F, .5F, 1F), color(.77F, .6F, 1F), pulse(phase));
      case CLOUDS -> blend(color(.58F, .25F, .9F), color(.62F, .15F, 1F), pulse(phase));
      case SPACE -> blend(color(.66F, .45F, .9F), color(.72F, .55F, .95F), pulse(phase));
      case FALLINGSTARS -> blend(color(.14F, .55F, 1F), color(.18F, .65F, .95F), pulse(phase));
      case LAVALAMP -> blend(color(.04F, .7F, 1F), color(.08F, .8F, .95F), pulse(phase));
      case DEPTHMAP -> blend(color(.60F, .15F, .75F), color(.56F, .1F, .7F), pulse(phase));
      case FOGGYCLOUDS -> blend(color(.55F, .55F, .92F), color(.52F, .45F, .88F), pulse(phase));
      case GLASSLIQUID -> blend(color(.50F, .65F, .90F), color(.49F, .75F, .85F), pulse(phase));
      case METALCLOUDS -> blend(color(.58F, .08F, .82F), color(.60F, .05F, .78F), pulse(phase));
      case SMOKISH -> blend(color(.78F, .55F, .80F), color(.75F, .45F, .76F), pulse(phase));
      case SPLIT -> blend(color(.35F, .60F, .82F), color(.32F, .70F, .78F), pulse(phase));
      case WAVYFOG -> blend(color(.95F, .50F, .82F), color(.92F, .42F, .78F), pulse(phase));
      case WAVYPATTERN -> blend(color(.08F, .70F, .92F), color(.10F, .80F, .88F), pulse(phase));
      case CLASSIC -> color(.535F, .7F, 1F);
    };
  }

  static float[] secondary(HaloInterfaceStyle style, double phase) {
    return switch (style) {
      case MANA -> color(.50F + wave(phase + .25D) * .015F, .28F, 1F);
      case CORPOREA -> color(.11F + wave(phase + .55D) * .025F, .82F, 1F);
      case BOTANIA -> blend(color(.12F, .72F, 1F), color(.34F, .58F, 1F), pulse(phase + .3D));
      case CLOUDS -> blend(color(.60F, .2F, .85F), color(.56F, .3F, .95F), pulse(phase + .3D));
      case SPACE -> blend(color(.70F, .35F, .85F), color(.64F, .28F, .8F), pulse(phase + .3D));
      case FALLINGSTARS -> blend(color(.12F, .45F, .9F), color(.16F, .5F, .85F), pulse(phase + .3D));
      case LAVALAMP -> blend(color(.02F, .6F, .85F), color(.06F, .5F, .8F), pulse(phase + .3D));
      case DEPTHMAP -> blend(color(.62F, .1F, .65F), color(.58F, .05F, .6F), pulse(phase + .3D));
      case FOGGYCLOUDS -> blend(color(.57F, .42F, .82F), color(.54F, .35F, .78F), pulse(phase + .3D));
      case GLASSLIQUID -> blend(color(.51F, .55F, .82F), color(.50F, .48F, .78F), pulse(phase + .3D));
      case METALCLOUDS -> blend(color(.60F, .05F, .72F), color(.57F, .03F, .68F), pulse(phase + .3D));
      case SMOKISH -> blend(color(.80F, .42F, .72F), color(.77F, .38F, .68F), pulse(phase + .3D));
      case SPLIT -> blend(color(.36F, .50F, .74F), color(.33F, .45F, .70F), pulse(phase + .3D));
      case WAVYFOG -> blend(color(.96F, .38F, .72F), color(.93F, .32F, .68F), pulse(phase + .3D));
      case WAVYPATTERN -> blend(color(.09F, .60F, .82F), color(.11F, .55F, .78F), pulse(phase + .3D));
      case CLASSIC -> color(.52F, .42F, 1F);
    };
  }

  static float[] accent(HaloInterfaceStyle style, double phase) {
    return switch (style) {
      case MANA -> color(.56F + wave(phase) * .025F, .2F, 1F);
      case CORPOREA -> color(.08F + wave(phase + .7D) * .025F, .9F, 1F);
      case BOTANIA -> blend(color(.09F, .72F, 1F), color(.82F, .45F, 1F), pulse(phase + .65D));
      case CLOUDS -> color(.64F, .1F, 1F);
      case SPACE -> color(.58F, .2F, 1F);
      case FALLINGSTARS -> color(.10F, .7F, 1F);
      case LAVALAMP -> color(.06F, .75F, 1F);
      case DEPTHMAP -> color(.58F, .05F, .8F);
      case FOGGYCLOUDS -> color(.53F, .65F, .98F);
      case GLASSLIQUID -> color(.49F, .80F, .96F);
      case METALCLOUDS -> color(.56F, .04F, .92F);
      case SMOKISH -> color(.76F, .60F, .88F);
      case SPLIT -> color(.33F, .72F, .90F);
      case WAVYFOG -> color(.94F, .55F, .88F);
      case WAVYPATTERN -> color(.10F, .85F, .96F);
      case CLASSIC -> color(.55F, .38F, 1F);
    };
  }

  static float[] particle(HaloInterfaceStyle style, double phase) {
    return switch (style) {
      case MANA -> blend(primary(style, phase), color(.55F, .12F, 1F), pulse(phase + .15D));
      case CORPOREA -> blend(primary(style, phase), accent(style, phase + .35D), pulse(phase + .45D));
      case BOTANIA -> blend(blend(primary(style, phase), secondary(style, phase), pulse(phase)), accent(style, phase), pulse(phase + .5D));
      case CLOUDS -> blend(color(.58F, .3F, .9F), color(0F, 0F, .85F), pulse(phase + .2D));
      case SPACE -> blend(color(.66F, .5F, .95F), color(.58F, .15F, 1F), pulse(phase + .35D));
      case FALLINGSTARS -> blend(color(.14F, .6F, 1F), color(.18F, .4F, .9F), pulse(phase + .25D));
      case LAVALAMP -> blend(color(.04F, .65F, 1F), color(.08F, .5F, .9F), pulse(phase + .3D));
      case DEPTHMAP -> blend(color(.60F, .12F, .8F), color(.56F, .08F, .7F), pulse(phase + .4D));
      case FOGGYCLOUDS -> blend(color(.55F, .5F, .94F), color(.52F, .4F, .88F), pulse(phase + .25D));
      case GLASSLIQUID -> blend(color(.50F, .70F, .95F), color(.49F, .55F, .88F), pulse(phase + .3D));
      case METALCLOUDS -> blend(color(.58F, .06F, .88F), color(.60F, .04F, .80F), pulse(phase + .35D));
      case SMOKISH -> blend(color(.78F, .52F, .82F), color(.75F, .42F, .76F), pulse(phase + .3D));
      case SPLIT -> blend(color(.35F, .62F, .86F), color(.32F, .52F, .80F), pulse(phase + .3D));
      case WAVYFOG -> blend(color(.95F, .48F, .84F), color(.92F, .40F, .78F), pulse(phase + .3D));
      case WAVYPATTERN -> blend(color(.09F, .72F, .94F), color(.10F, .60F, .88F), pulse(phase + .3D));
      case CLASSIC -> primary(style, phase);
    };
  }

  static float[] tint(float[] color, float[] tint, float strength, float lift) {
    return new float[] {
        tintChannel(color[0], tint[0], strength, lift),
        tintChannel(color[1], tint[1], strength, lift),
        tintChannel(color[2], tint[2], strength, lift)
    };
  }

  private static float[] color(float hue, float saturation, float value) {
    return MathUtil.hsvToRGB(wrap(hue), clamp(saturation), clamp(value));
  }

  private static float[] blend(float[] first, float[] second, float amount) {
    float inverse = 1F - amount;
    return new float[] {
        first[0] * inverse + second[0] * amount,
        first[1] * inverse + second[1] * amount,
        first[2] * inverse + second[2] * amount
    };
  }

  private static float pulse(double phase) {
    return (float) ((Math.sin(phase * Math.PI * 2D) + 1D) * .5D);
  }

  private static float wave(double phase) {
    return (float) Math.sin(phase * Math.PI * 2D);
  }

  private static float wrap(float value) {
    value %= 1F;
    return value < 0F ? value + 1F : value;
  }

  private static float tintChannel(float color, float tint, float strength, float lift) {
    return clamp(color * (1F - strength + tint * strength) + tint * lift);
  }

  private static float clamp(float value) {
    return Math.max(0F, Math.min(1F, value));
  }
}