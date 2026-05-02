#version 150

// Halo DEPTHMAP background shader
// texCoord0.x = world azimuth / (2*PI), texCoord0.y = height 0..1.
// Output is white/grayscale; vertex colour provides the tint.
// Adapted from depthmap.glsl (animated Perlin contour map).

uniform float GameTime;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// ---- Perlin noise (from depthmap.glsl) ----

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453) * 100.0;
}

vec2 getBlock(float res, vec2 loc) {
    return floor(loc / res) * res;
}

vec2 getVector(float res, vec2 loc) {
    float b = rand(getBlock(res, loc)) * 100.0;
    return vec2(sin(b), cos(b));
}

float interpolate(float t) {
    return 6.0 * pow(t, 5.0) - 15.0 * pow(t, 4.0) + 10.0 * pow(t, 3.0);
}

float perlin(float res, vec2 loc) {
    vec2 localCoord = (getBlock(res, loc + res) - loc) / res;
    vec2 vecTL = getVector(res, loc);
    vec2 vecTR = getVector(res, loc + vec2(res, 0.0));
    vec2 vecBL = getVector(res, loc + vec2(0.0, -res));
    vec2 vecBR = getVector(res, loc + vec2(res,  -res));
    vec2 interp = vec2(interpolate(localCoord.x), interpolate(localCoord.y));
    localCoord = localCoord * 2.0 - 1.0;
    float dottl = (dot(vecTL, localCoord - vec2( 1.0, -1.0)) + 1.4) / 2.3;
    float dottr = (dot(vecTR, localCoord - vec2(-1.0, -1.0)) + 1.4) / 2.3;
    float dotbl = (dot(vecBL, localCoord - vec2( 1.0,  1.0)) + 1.4) / 2.3;
    float dotbr = (dot(vecBR, localCoord - vec2(-1.0,  1.0)) + 1.4) / 2.3;
    float horizTop    = interp.x * dottl + (1.0 - interp.x) * dottr;
    float horizBottom = interp.x * dotbl + (1.0 - interp.x) * dotbr;
    float vertical    = interp.y * horizBottom + (1.0 - interp.y) * horizTop;
    return vertical / 1.1;
}

void main() {
    float time = GameTime * 1200.0;

    // Map texCoord0 into a pixel-like space that matches the original resolution=100 Perlin scale.
    // Scale x by 3.2 to correct the horizontal aspect ratio (same as other halo shaders).
    vec2 loc = texCoord0 * vec2(3.2 * 300.0, 300.0);

    // Slowly drifting origin — same parameters as original.
    float moveAmount = 1000.0;
    float moveSpeed  = 35.0;
    float ox = perlin(moveSpeed, vec2(time))         * moveAmount;
    float oy = perlin(moveSpeed, vec2(time + 500.0)) * moveAmount;
    vec2  shifted = loc + vec2(ox, oy);

    // Two-octave Perlin: coarse + fine detail.
    float p1 = perlin(100.0, shifted);
    float p3 = perlin(100.0, 4.0 * shifted) / 16.0;
    float value = p1 + p3;

    // Contour rings: mod wraps value into repeating bands, /12 keeps brightness subtle.
    float lum = mod(value * 10.0, 1.0) / 12.0;

    // Top/bottom alpha fade
    float vFade = smoothstep(0.0, 0.18, texCoord0.y) * smoothstep(1.0, 0.82, texCoord0.y);

    fragColor = vec4(vec3(lum), vFade) * vertexColor * ColorModulator;
}
