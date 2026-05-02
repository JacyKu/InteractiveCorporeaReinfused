#version 150

// Halo WAVYFOG background shader
// texCoord0.x is world azimuth / (2 * PI), texCoord0.y is height 0..1.
// 4-iteration sine warp producing gently rolling fog waves (from wavyfog.glsl).
// Output is white/grayscale -- vertex color provides the tint.

uniform float GameTime;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float time = GameTime * 1200.0;

    // World-pinned UV with aspect correction
    vec2 p;
    p.x = (texCoord0.x * 3.2 - 0.5) * 1.0;
    p.y = (texCoord0.y - 0.5) * 1.0;

    // 4-iteration sine warp (matching wavyfog.glsl)
    for (int i = 1; i < 5; i++) {
        float fi = float(i);
        p += sin(p.yx * vec2(1.6, 1.1) * (fi + 11.0) + time * fi * vec2(3.4, 0.5) / 10.0) * 0.1;
    }

    float lum = (abs(sin(p.y) + sin(p.x))) * 0.5;
    lum = clamp(lum, 0.0, 1.0);

    float vFade = smoothstep(0.0, 0.18, texCoord0.y) * smoothstep(1.0, 0.82, texCoord0.y);

    fragColor = vec4(vec3(lum), vFade) * vertexColor * ColorModulator;
}
