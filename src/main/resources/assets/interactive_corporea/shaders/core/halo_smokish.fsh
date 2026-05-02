#version 150

// Halo SMOKISH background shader
// texCoord0.x is world azimuth / (2 * PI), texCoord0.y is height 0..1.
// Aurora-layer + glitchy-particle smoke effect (adapted from smokish.glsl).
// Output is white/grayscale -- vertex color provides the tint.

uniform float GameTime;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// ---- Noise helpers ----

float hash(float n) {
    return fract(sin(n) * 78757.5757 + cos(n) * 71767.8727);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(hash(i.x + hash(i.y)),       hash(i.x + 1.0 + hash(i.y)),       u.x),
        mix(hash(i.x + hash(i.y + 1.0)), hash(i.x + 1.0 + hash(i.y + 1.0)), u.x),
        u.y);
}

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

// ---- Glitchy particles (grayscale) ----

float glitchyParticles(vec2 uv, float t) {
    float noiseVal = noise(uv * 1000.0 + t * 10.0);
    vec2 jittered = uv + mix(vec2(0.0), vec2(sin(noiseVal), cos(noiseVal)), 0.1);
    return clamp(1.0 - smoothstep(0.0, 0.99, length(fract(jittered) - 0.5)), 0.0, 1.0);
}

// ---- Aurora layer (grayscale) ----

float auroraLayer(vec2 uv, float t, float speed, float intensity) {
    float lt = t * speed;
    vec2 scaleXY = vec2(2.0 + sin(lt) * 0.5, 5.0 + cos(lt) * 0.5);
    vec2 p = uv * scaleXY + lt * vec2(2.0, -2.0);
    float n = noise(p + noise(p + lt));
    float aurora = smoothstep(0.0, 0.05, n - uv.y) * (1.0 - smoothstep(0.0, 0.2, n - uv.y));
    return aurora * intensity;
}

void main() {
    float time = GameTime * 1200.0;

    // World-pinned UV with aspect correction
    vec2 uv;
    uv.x = texCoord0.x * 3.2;
    uv.y = texCoord0.y;

    // Jitter
    uv += vec2(rand(uv), rand(uv)) * 0.005;

    float lum = 0.0;

    // Glitchy particles
    lum += glitchyParticles(uv, time) * 0.4;

    // Four aurora layers (grayscale weights matching original color magnitudes)
    lum += auroraLayer(uv, time, 0.55, 0.12);
    lum += auroraLayer(uv, time, 0.10, 0.21);
    lum += auroraLayer(uv, time, 0.15, 0.17);
    lum += auroraLayer(uv, time, 0.07, 0.13);

    // Background gradient (faint base brightness)
    lum += 0.08 * (1.0 - smoothstep(0.0, 2.0, uv.y));
    lum += 0.05 * (1.0 - smoothstep(0.0, 1.0, uv.y));

    lum = clamp(lum, 0.0, 1.0);

    float vFade = smoothstep(0.0, 0.18, texCoord0.y) * smoothstep(1.0, 0.82, texCoord0.y);

    fragColor = vec4(vec3(lum), vFade) * vertexColor * ColorModulator;
}
