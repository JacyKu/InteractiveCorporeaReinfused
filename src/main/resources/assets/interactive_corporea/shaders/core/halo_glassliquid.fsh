#version 150

// Halo GLASSLIQUID background shader
// texCoord0.x is world azimuth / (2 * PI), texCoord0.y is height 0..1.
// Domain-warped Perlin FBM creates flowing "glass liquid" patterns
// (adapted from glassliquid.glsl, credit: kamend.com).
// Output is white/grayscale -- vertex color provides the tint.

uniform float GameTime;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// ---- Classic Perlin noise ----

vec4 mod289(vec4 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 permute(vec4 x) { return mod289(((x * 34.0) + 1.0) * x); }
vec4 taylorInvSqrt(vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }
vec2 fade(vec2 t) { return t * t * t * (t * (t * 6.0 - 15.0) + 10.0); }

float cnoise(vec2 P) {
    vec4 Pi = floor(P.xyxy) + vec4(0.0, 0.0, 1.0, 1.0);
    vec4 Pf = fract(P.xyxy) - vec4(0.0, 0.0, 1.0, 1.0);
    Pi = mod289(Pi);
    vec4 ix = Pi.xzxz;
    vec4 iy = Pi.yyww;
    vec4 fx = Pf.xzxz;
    vec4 fy = Pf.yyww;
    vec4 i = permute(permute(ix) + iy);
    vec4 gx = fract(i * (1.0 / 41.0)) * 2.0 - 1.0;
    vec4 gy = abs(gx) - 0.5;
    gx -= floor(gx + 0.5);
    vec2 g00 = vec2(gx.x, gy.x);
    vec2 g10 = vec2(gx.y, gy.y);
    vec2 g01 = vec2(gx.z, gy.z);
    vec2 g11 = vec2(gx.w, gy.w);
    vec4 norm = taylorInvSqrt(vec4(dot(g00,g00), dot(g01,g01), dot(g10,g10), dot(g11,g11)));
    g00 *= norm.x; g01 *= norm.y; g10 *= norm.z; g11 *= norm.w;
    float n00 = dot(g00, vec2(fx.x, fy.x));
    float n10 = dot(g10, vec2(fx.y, fy.y));
    float n01 = dot(g01, vec2(fx.z, fy.z));
    float n11 = dot(g11, vec2(fx.w, fy.w));
    vec2 fade_xy = fade(Pf.xy);
    vec2 n_x = mix(vec2(n00, n01), vec2(n10, n11), fade_xy.x);
    return 2.3 * mix(n_x.x, n_x.y, fade_xy.y);
}

// FBM with fixed 5 octaves (enough detail for a real-time halo background)
float fbm(vec2 P, float lacunarity, float gain) {
    float sum = 0.0;
    float amp = 1.0;
    vec2 pp = P;
    for (int i = 0; i < 5; i++) {
        amp *= gain;
        sum += amp * cnoise(pp);
        pp *= lacunarity;
    }
    return sum;
}

// Domain-warped FBM pattern (same warp structure as glassliquid.glsl)
float glassPattern(vec2 p, float t) {
    float l = 2.3;
    float g = 0.4;
    vec2 q = vec2(
        fbm(p + vec2(t,       t      ), l, g),
        fbm(p + vec2(5.2 * t, 1.3 * t), l, g)
    );
    vec2 r = vec2(
        fbm(p + 4.0 * q + vec2(1.7, 9.2), l, g),
        fbm(p + 4.0 * q + vec2(8.3, 2.8), l, g)
    );
    return fbm(p + 4.0 * r, l, g);
}

void main() {
    float time = GameTime * 1200.0 * 0.012;

    // World-pinned UV with aspect correction
    vec2 uv;
    uv.x = texCoord0.x * 3.2;
    uv.y = texCoord0.y;
    vec2 p = uv * 2.2 - vec2(0.4, 1.1);

    float c = glassPattern(p, time);

    // Map from signed noise range to [0,1] brightness
    float lum = clamp(c * 1.5 + 0.5, 0.0, 1.0);

    float vFade = smoothstep(0.0, 0.18, texCoord0.y) * smoothstep(1.0, 0.82, texCoord0.y);

    fragColor = vec4(vec3(lum), vFade) * vertexColor * ColorModulator;
}
