#version 150

// Halo FOGGYCLOUDS background shader
// texCoord0.x is world azimuth / (2 * PI), texCoord0.y is height 0..1.
// FBM domain-warping cloud (adapted from foggyclouds.glsl by hornidev).
// Output is white/grayscale -- vertex color provides the tint.

#define NUM_OCTAVES 6

uniform float GameTime;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// ---- FBM-based fog cloud noise ----

float random(vec2 pos) {
    return fract(sin(dot(pos, vec2(13.9898, 78.233))) * 43758.5453123);
}

float noise(vec2 pos) {
    vec2 i = floor(pos);
    vec2 f = fract(pos);
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 pos, float time) {
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100.0);
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));
    for (int i = 0; i < NUM_OCTAVES; i++) {
        float dir = mod(float(i), 2.0) > 0.5 ? 1.0 : -1.0;
        v += a * noise(pos - 0.18 * dir * time);
        pos = rot * pos * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}

void main() {
    float time = GameTime * 1200.0;

    // Map world-pinned UV to FBM sample space, with aspect correction
    vec2 uv;
    uv.x = texCoord0.x * 3.2; // compensate for narrow visible arc
    uv.y = texCoord0.y;
    vec2 p = uv * vec2(4.0, 3.0) + vec2(2.5, 1.5);

    // Domain-warped FBM (same structure as original foggyclouds.glsl)
    vec2 q;
    q.x = fbm(p, time);
    q.y = fbm(p + vec2(1.0), time);

    vec2 r;
    r.x = fbm(p + q + vec2(1.85, 1.2), time);
    r.y = fbm(p + q + vec2(8.43, 2.8), time);

    float f = fbm(p + r, time);

    // Grayscale brightness -- vertex color provides the tint
    float lum = clamp(f * f * f + 0.9 * f, 0.0, 1.0);

    float vFade = smoothstep(0.0, 0.18, texCoord0.y) * smoothstep(1.0, 0.82, texCoord0.y);

    fragColor = vec4(vec3(lum), vFade) * vertexColor * ColorModulator;
}
