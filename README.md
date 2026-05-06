# Interactive Corporea Reinfused

Interactive Corporea Reinfused is an add-on mod for [Botania](https://www.curseforge.com/minecraft/mc-mods/botania "Botania") that makes the Corporea system easier to use, more immersive, and more magical while staying faithful to [Botania's design philosophy](https://botaniamod.net/ "Botania") of avoiding full-screen GUIs.

> Ported to: Forge 1.20.1-47.4.10  
> Recent additions and fixes are tracked in [CHANGELOG.md](CHANGELOG.md).

## Highlights

- In-world **Requesting Halo** interface for browsing and requesting items from a Corporea network.
- Halo modules for **search**, **HUD details**, **auto updates**, **anchoring**, **item return**, and **range expansion**.
- Animated **shader-based halo backgrounds** unlocked by Botania flowers, plus legacy petal styles.
- **Oculus compatibility** and **dynamic lighting** intergration.

## In-Game Screenshot

![Requesting Halo screenshot](https://media.forgecdn.net/attachments/description/null/description_b85727f0-8be8-4cc6-9c9e-18062ee54bbf.png)

## Halo Background Styles

> Shader sources were adapted from [GLSL Sandbox](https://glslsandbox.com) ([GitHub](https://github.com/mrdoob/glsl-sandbox)).

Craft a **Requesting Halo** with a Botania **Mystical Flower** to change its background shader. Each flower color unlocks a different style:

| Mystical Flower | Style Name | Description |
| --- | --- | --- |
| White | Clouds | Layered noise clouds |
| Blue | Space | Volumetric star field |
| Yellow | Falling Stars | 3D fractal fly-through |
| Red | Aurora | Aurora borealis |
| Gray | Depth Map | Perlin contour rings |
| Light Blue | Foggy Clouds | FBM domain-warped fog |
| Cyan | Glass Liquid | Flowing glass-liquid warp |
| Light Gray | Metal Clouds | Metallic sine-warp bands |
| Purple | Smokish | Aurora smoke with particles |
| Green | Split | Fine rippling split bands |
| Pink | Wavy Fog | Gently rolling fog waves |
| Orange | Wavy Pattern | Sine interference pattern |

Legacy **petal-based** styles are also available:

| Petal | Style |
| --- | --- |
| Light Blue petal | Classic |
| Cyan petal | Mana |
| Purple petal | Corporea |
| Lime petal | Botania |

## Known Issues

- The rainbow glow used in the Requesting Halo and in the Quantization Device does not display the rays correctly.

## Roadmap

- Add a toggle to keep the Halo aligned with the player view angle.
- Fix player spinning when turning around and unanchoring the Halo.