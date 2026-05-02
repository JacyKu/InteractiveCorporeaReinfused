#version 150

// Halo WAVYPATTERN background shader
// texCoord0.x is world azimuth / (2 * PI), texCoord0.y is height 0..1.
// normsin-based interference of two perpendicular standing waves (from wavypattern.glsl).
// Output is white/grayscale -- vertex color provides the tint.

uniform float GameTime;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float normsin(float x) {
    return (sin(x) + 1.0) / 11.0;
}

float interpolate(float x, float minV, float maxV) {
    return x * maxV + (1.0 - x) * minV;
}

void main() {
    float time = GameTime * 1200.0 * 2.0;

    // World-pinned position with aspect correction
    float px = texCoord0.x * 3.2;
    float py = texCoord0.y;

    // Interference pattern (from wavypattern.glsl, mouse replaced by 0.0)
    float lum = normsin(
        30.0 * px + interpolate(normsin(25.0 * py), 5.0, 25.0) +
        30.0 * py + interpolate(normsin(25.0 * px), 5.0, 25.0) +
        2.0 * time
    );

    lum = clamp(lum, 0.0, 1.0);

    float vFade = smoothstep(0.0, 0.18, texCoord0.y) * smoothstep(1.0, 0.82, texCoord0.y);

    fragColor = vec4(vec3(lum), vFade) * vertexColor * ColorModulator;
}
