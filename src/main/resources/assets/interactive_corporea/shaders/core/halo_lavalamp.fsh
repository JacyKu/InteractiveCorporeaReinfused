#version 150

// Halo LAVALAMP (aurora borealis) background shader
// texCoord0.x = world azimuth / (2*PI), texCoord0.y = height 0..1.
// Output is white/grayscale; vertex colour provides the tint.
// Adapted from lavalamp.glsl (aurora shader).

uniform float GameTime;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// ---- Noise ----
float hash(float n) {
    return fract(sin(n) * 78757.5757 + cos(n) * 71767.8727);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i.x + hash(i.y)),          hash(i.x + 1.0 + hash(i.y)),          u.x),
               mix(hash(i.x + hash(i.y + 1.0)),    hash(i.x + 1.0 + hash(i.y + 1.0)),    u.x), u.y);
}

// Aurora band — returns grayscale intensity; vertex colour provides the hue.
float auroraLayer(vec2 uv, float speed, float intensity, float seed, float t) {
    vec2 p = uv * 2.0 + t * speed * vec2(2.0, -2.0);
    float n = noise(p + noise(p + vec2(seed, seed) + t * speed));
    float aurora = smoothstep(0.0, 0.1, n - uv.y) * (1.0 - smoothstep(0.0, 0.5, n - uv.y));
    return aurora * intensity;
}

void main() {
    float time = GameTime * 1200.0;

    // Use texCoord0 directly: y = 0..1 height, x = world azimuth arc.
    // Scale x to ~match y range so the aurora bands are not stretched.
    vec2 uv = vec2(texCoord0.x * 3.2, texCoord0.y);

    float lum = 0.0;
    lum += auroraLayer(uv, 0.05, 0.30, 0.0,  time);
    lum += auroraLayer(uv, 0.10, 0.40, 1.3,  time);
    lum += auroraLayer(uv, 0.15, 0.30, 2.7,  time);
    lum += auroraLayer(uv, 0.07, 0.20, 4.1,  time);

    // Subtle dark-sky gradient at the bottom (grayscale)
    lum += (1.0 - smoothstep(0.0, 1.0, uv.y)) * 0.08;

    // Top/bottom alpha fade
    float vFade = smoothstep(0.0, 0.18, texCoord0.y) * smoothstep(1.0, 0.82, texCoord0.y);

    fragColor = vec4(vec3(lum), vFade) * vertexColor * ColorModulator;
}
