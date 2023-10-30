#version 330 core
out vec4 FragColor;

in vec2 TexCoord;

uniform vec3 diffuseColour;
uniform sampler2D mainTexture1;

void main() {
    FragColor = texture(mainTexture1, TexCoord) * vec4(diffuseColour, 1.0f);
}