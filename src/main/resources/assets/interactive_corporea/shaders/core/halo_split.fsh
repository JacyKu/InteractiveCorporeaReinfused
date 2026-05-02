#version 150

// Halo SPLIT background shader
// texCoord0.x is world azimuth / (2 * PI), texCoord0.y is height 0..1.
// Iterative sine-warp with 18 octaves producing fine split-band ripples.
// Output is white/grayscale -- vertex color provides the tint.

uniform float GameTime;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float time = GameTime * 1200.0;

    // World-pinned UV with aspect correction
    vec2 pos;
    pos.x = texCoord0.x * 3.2 * 4.0;
    pos.y = texCoord0.y * 4.0;

    // Iterative sine warp -- 18 octaves (n=2..19, matching split.glsl)
    for (int n = 2; n < 20; n++) {
        float i = float(n);
        pos += vec2(
            0.3 / i * sin(i * pos.y * i + time / 10.0 + 0.9 * i) + 0.5,
            0.3 / i * sin(i * pos.x * i + time / 10.0 + 0.9 * i) + 1.5
        );
    }

    // Collapse the two original channels to a single luminance
    float r = 0.5 * cos(pos.x) + 0.6;
    float g = r * cos(pos.x) + 0.8;
    float lum = clamp((r + g) * 0.5, 0.0, 1.0);

    float vFade = smoothstep(0.0, 0.18, texCoord0.y) * smoothstep(1.0, 0.82, texCoord0.y);

    fragColor = vec4(vec3(lum), vFade) * vertexColor * ColorModulator;
}
