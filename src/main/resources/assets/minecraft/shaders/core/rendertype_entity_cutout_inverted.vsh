#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vec4 worldPos = ModelViewMat * vec4(Position, 1.0);
    vertexDistance = (FogShape == 0)
        ? length(worldPos.xz)
        : length(worldPos.xyz);

    vec4 lightColor0 = texelFetch(Sampler1, UV1 / 16, 0);
    vec4 lightColor1 = texelFetch(Sampler2, UV2 / 16, 0);
    vertexColor = min(lightColor0 + lightColor1, vec4(1.0)) * Color;

    texCoord0 = UV0;
}
