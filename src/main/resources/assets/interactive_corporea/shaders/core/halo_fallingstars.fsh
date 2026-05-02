#version 150

// Halo FALLINGSTARS background shader
// texCoord0.x = world azimuth / (2*PI), texCoord0.y = height 0..1.
// UV is pinned to world directions (skybox-static) while the star field drifts over time.
// Adapted from fallingstars.glsl.

uniform float GameTime;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float time = GameTime * 1200.0; // convert to real seconds (1 MC day = 1200 s)

    // Map texCoord0 (0..1) → centred UV, then correct the horizontal aspect ratio.
    // texCoord0.x spans ~0.25 of the full-circle arc, so multiply x to balance with y.
    vec2 uv = texCoord0 * 2.0 - 1.0;
    uv.x *= 3.2;

    vec3 dir = vec3(uv, 1.0);

    // Fixed rotation (same as original — rot angle is constant, motion comes from translation)
    float a1 = 1.0;
    mat2 rot = mat2(cos(a1), sin(a1), -sin(a1), cos(a1));
    dir.xz *= rot;
    dir.xy *= rot;

    // Slow drift: original used (0.0025, 0.03, -2) per shadertoy-second.
    // Scaled down ~10x for a gentle falling-stars effect.
    vec3 from = vec3(0.0, 0.0, 0.0);
    from += vec3(0.00025 * time, 0.003 * time, -2.0);
    from.xz *= rot;
    from.xy *= rot;

    float s    = 0.1;
    float fade = 0.07;
    vec3  v    = vec3(0.4);

    for (int r = 0; r < 10; r++) {
        vec3 p = from + s * dir * 1.5;
        p = abs(vec3(0.750) - mod(p, vec3(0.750 * 2.0)));
        p.x += float(r * r) * 0.01;
        p.y += float(r) * 0.02;

        float pa = 0.0;
        float a  = 0.0;
        for (int i = 0; i < 12; i++) {
            p  = abs(p) / dot(p, p) - 0.340;
            a += abs(length(p) - pa * 0.2);
            pa = length(p);
        }

        a *= a * a * 2.0;
        // Uniform s so brightness is white — vertex colour provides the tint.
        v    += vec3(s) * a * 0.0017 * fade;
        fade *= 0.960;
        s    += 0.110;
    }

    // Fully desaturate so vertex colour provides the tint.
    v = vec3(length(v));

    // Top/bottom alpha fade to blend with panel edges
    float vFade = smoothstep(0.0, 0.18, texCoord0.y) * smoothstep(1.0, 0.82, texCoord0.y);

    fragColor = vec4(v * 0.01, vFade) * vertexColor * ColorModulator;
}
