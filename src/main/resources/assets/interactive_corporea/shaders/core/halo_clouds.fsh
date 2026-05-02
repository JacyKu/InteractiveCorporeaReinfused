#version 150

// Halo CLOUDS background shader
// texCoord0.x is world azimuth / (2 * PI), texCoord0.y is height 0..1.
// The sample stays pinned to world directions while the clouds still animate over time.

uniform float GameTime;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// ---- Clouds noise (from clouds.glsl by Trisomie21) ----

vec4 textureRND2D(vec2 uv) {
    uv = floor(fract(uv) * 1e3);
    float v = uv.x + uv.y * 1e3;
    return fract(1e5 * sin(vec4(v * 1e-2, (v + 1.) * 1e-2, (v + 1e3) * 1e-2, (v + 1e3 + 1.) * 1e-2)));
}

float noise(vec2 p) {
    vec2 f = fract(p * 1e3);
    vec4 r = textureRND2D(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(r.x, r.y, f.x), mix(r.z, r.w, f.x), f.y);
}

float cloud(vec2 p) {
    float v = 0.0;
    v += noise(p * 1.0)  * 0.50000;
    v += noise(p * 2.0)  * 0.20000;
    v += noise(p * 4.0)  * 0.12500;
    v += noise(p * 8.0)  * 0.06250;
    v += noise(p * 16.0) * 0.03125;
    return v * v * v;
}

void main() {
    // texCoord0.x = worldAzimuth / (2*PI), spanning ~0.25 over the visible 90-degree arc.
    // texCoord0.y = height (0 = bottom, 1 = top).
    float time = GameTime * 1200.0;

    vec2 p = vec2(texCoord0.x * 0.4, texCoord0.y * 0.1);

    // All cloud layers added uniformly so output is white/grayscale -- vertex colour provides the tint.
    float c = 0.0;
    c += cloud(p * 0.3 + time * 0.0002) * 0.6;
    c += cloud(p * 0.2 + time * 0.0002) * 0.8;
    c += cloud(p * 0.1 + time * 0.0002) * 1.0;

    float verticalFade = smoothstep(0.0, 0.22, texCoord0.y) * (1.0 - smoothstep(0.78, 1.0, texCoord0.y));

    // Apply vertex alpha (for open/close fade) and petal colour tint
    fragColor = vec4(vec3(c), verticalFade) * vec4(vertexColor.rgb, vertexColor.a) * ColorModulator;
}
