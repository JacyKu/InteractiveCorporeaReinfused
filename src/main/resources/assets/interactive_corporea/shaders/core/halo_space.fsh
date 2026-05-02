#version 150

// Halo SPACE background shader
// texCoord0.x = world azimuth / (2*PI), texCoord0.y = height 0..1.
// UV is pinned to world directions (skybox-static) while the star field animates over time.
// Adapted from space.glsl (Shadertoy).

uniform float GameTime;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// ---- Constants ----
#define iterations    14
#define formuparam2   0.79
#define volsteps      5
#define stepsize      0.390
#define zoom          0.900
#define tile          0.850
#define brightness    0.003
#define distfading    0.560
#define saturation    0.800
#define transverseSpeed (zoom * 2.0)
#define cloudDensity  0.11

// ---- Nebula cloud field (single unrolled iteration) ----
float field(in vec3 p, float t) {
    float strength = 7.0 + 0.03 * log(1.0e-6 + fract(sin(t) * 4373.11));
    float prev = 0.0;
    float mag  = dot(p, p);
    p = abs(p) / mag + vec3(-0.5, -0.8 + 0.1 * sin(t * 0.7 + 2.0), -1.1 + 0.3 * cos(t * 0.3));
    // w = exp(-0/7) = 1.0 (single-pass version of the original loop)
    float accum = exp(-strength * pow(abs(mag - prev), 2.3));
    return max(0.0, 5.0 * accum - 0.7);
}

void main() {
    float time = GameTime * 1200.0;

    // Map texCoord0 (0..1) → (-1..+1), then correct the aspect ratio.
    // texCoord0.x spans only ~0.25 of the full-circle UV (the visible 90° arc),
    // so after *2-1 it covers ±0.5 while texCoord0.y covers ±1.0.
    // Multiply x by 3.2 so both axes span ±1 and the ray directions are isotropic.
    vec2 uv = texCoord0 * 2.0 - 1.0;
    uv.x *= 3.2;

    // Rotation matrices (time-driven, same as original but ~5x slower)
    float a_xz = 0.9;
    float a_yz = -0.6;
    float a_xy = 0.9 + time * 0.016;
    mat2 rot_xz = mat2( cos(a_xz),  sin(a_xz), -sin(a_xz), cos(a_xz));
    mat2 rot_yz = mat2( cos(a_yz),  sin(a_yz), -sin(a_yz), cos(a_yz));
    mat2 rot_xy = mat2( cos(a_xy),  sin(a_xy), -sin(a_xy), cos(a_xy));

    // Slow auto-panning (replaces mouse input from original)
    vec2 mouse2 = vec2(sin(time) / 48.0, cos(time) / 48.0);

    vec3 dir  = vec3(uv * zoom, 1.0);
    vec3 from = vec3(0.0);
    from.x -= 5.0 * (mouse2.x - 0.5);
    from.y -= 5.0 * (mouse2.y - 0.5);

    // Fly-through motion
    float speed = 0.002 * cos(time * 0.004 + 3.1415926 / 4.0);
    vec3 forward = vec3(0.0, 0.0, 1.0);
    from.x += transverseSpeed * cos(0.003 * time) + 0.0003 * time;
    from.y += transverseSpeed * sin(0.003 * time) + 0.0003 * time;
    from.z += 0.00001 * time;

    dir.xy     *= rot_xy;
    forward.xy *= rot_xy;
    dir.xz     *= rot_xz;
    forward.xz *= rot_xz;
    dir.yz     *= rot_yz;
    forward.yz *= rot_yz;

    from.xy *= -rot_xy;
    from.xz *=  rot_xz;
    from.yz *=  rot_yz;

    float zooom       = (time - 3311.0) * speed;
    from             += forward * zooom;
    float sampleShift = mod(zooom, stepsize);
    float zoffset     = -sampleShift;
    sampleShift      /= stepsize;

    // ---- Volumetric ray-march ----
    float s  = 0.24;
    float s3 = s + stepsize * 0.5;
    vec3  v       = vec3(0.0);
    vec3  backCol = vec3(0.0);

    for (int r = 0; r < volsteps; r++) {
        vec3 p2 = from + (s  + zoffset) * dir;
        vec3 p3 = from + (s3 + zoffset) * dir;

        p2 = abs(vec3(tile) - mod(p2, vec3(tile * 2.0)));
        p3 = abs(vec3(tile) - mod(p3, vec3(tile * 2.0)));

        float t3 = field(p3, time);

        float pa = 0.0;
        float a  = 0.0;
        for (int i = 0; i < iterations; i++) {
            p2  = abs(p2) / dot(p2, p2) - formuparam2;
            float D = abs(length(p2) - pa);
            a  += (i > 7) ? min(12.0, D) : D;
            pa  = length(p2);
        }

        a *= a * a;

        float s1   = s + zoffset;
        float fade = pow(distfading, max(0.0, float(r) - sampleShift));
        v += vec3(fade);

        if (r == 0)            fade *= (1.0 - sampleShift);
        if (r == volsteps - 1) fade *= sampleShift;

        // Uniform s1*s1 so star brightness is white, not yellow.
        v       += vec3(s1 * s1) * a * brightness * fade;
        // Nebula uses raw field value uniformly across channels.
        backCol += vec3(t3) * fade;

        s  += stepsize;
        s3 += stepsize;
    }

    // Fully desaturate so vertex colour provides the tint.
    vec3 forCol = vec3(length(v)) * 0.01;

    // Nebula: uniform luminance, no blue/green bias.
    float nebulaLum = length(backCol) * cloudDensity * 0.5;
    vec3 spaceColor = clamp(forCol + vec3(nebulaLum), 0.0, 1.0);

    // Top/bottom alpha fade to blend with panel edges
    float vFade = smoothstep(0.0, 0.18, texCoord0.y) * smoothstep(1.0, 0.82, texCoord0.y);

    fragColor = vec4(spaceColor, vFade) * vertexColor * ColorModulator;
}
