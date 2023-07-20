#version 150

in vec4 vertexColor;
out vec4 fragColor;

uniform vec4 ColorModulator;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    fragColor = vec4(hsv2rgb(vertexColor.xyz), vertexColor.w) * ColorModulator;
}
