---
name: hd-graphics-handler
description: "Implements the HD graphics plugin: OpenGL 4.1 rendering pipeline, environment lighting/shadows, texture upscaling with HiDPI support, and GPU acceleration for the OSRS client."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# HD Graphics Handler

You implement the HD (high-definition) graphics upgrade for the OSRS client, modeled after RuneLite's GPU/HD plugin.

## OpenGL Pipeline

```kotlin
class GpuPlugin : RenderPlugin {
    private lateinit var gl: GL4
    private var sceneVbo = 0        // vertex buffer object for scene geometry
    private var sceneVao = 0        // vertex array object
    private var shadowFbo = 0       // shadow map framebuffer
    private var shadowMap = 0       // shadow map texture

    override fun startup(canvas: Canvas) {
        val context = GLProfile.getGL4()
        gl = context.gl.gL4
        setupShaders()
        setupBuffers()
        setupShadowMap()
    }

    override fun drawScene(scene: Scene, camera: Camera) {
        // 1. Shadow pass
        renderShadowMap(scene, camera)
        // 2. Geometry pass
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0)
        renderScene(scene, camera)
        // 3. UI pass (always software)
        renderWidgets()
    }
}
```

## GLSL Shaders

Vertex shader (`scene.vert`):
```glsl
#version 410

in vec3 position;
in vec2 uv;
in vec3 normal;
in vec4 color;

uniform mat4 viewProjection;
uniform mat4 model;
uniform mat4 lightSpaceMatrix;

out vec2 fragUv;
out vec3 fragNormal;
out vec4 fragColor;
out vec4 fragPosLightSpace;

void main() {
    vec4 worldPos = model * vec4(position, 1.0);
    gl_Position = viewProjection * worldPos;
    fragUv = uv;
    fragNormal = normalize(mat3(transpose(inverse(model))) * normal);
    fragColor = color;
    fragPosLightSpace = lightSpaceMatrix * worldPos;
}
```

Fragment shader (`scene.frag`):
```glsl
#version 410

in vec2 fragUv;
in vec3 fragNormal;
in vec4 fragColor;
in vec4 fragPosLightSpace;

uniform sampler2D textureAtlas;
uniform sampler2D shadowMap;
uniform vec3 lightDirection;
uniform vec3 ambientColor;
uniform float fogStart;
uniform float fogEnd;

out vec4 outColor;

float shadowCalc(vec4 lightSpacePos) {
    vec3 proj = lightSpacePos.xyz / lightSpacePos.w;
    proj = proj * 0.5 + 0.5;
    float closestDepth = texture(shadowMap, proj.xy).r;
    float currentDepth = proj.z;
    return currentDepth - 0.005 > closestDepth ? 0.5 : 1.0;
}

void main() {
    vec4 texColor = texture(textureAtlas, fragUv) * fragColor;
    float shadow = shadowCalc(fragPosLightSpace);
    float diffuse = max(dot(fragNormal, -lightDirection), 0.0);
    vec3 lighting = ambientColor + shadow * diffuse * vec3(1.0);
    // Apply fog
    float depth = gl_FragCoord.z / gl_FragCoord.w;
    float fogFactor = clamp((fogEnd - depth) / (fogEnd - fogStart), 0.0, 1.0);
    outColor = vec4(texColor.rgb * lighting, texColor.a);
    outColor.rgb = mix(vec3(0.5, 0.5, 0.5), outColor.rgb, fogFactor);
}
```

## Texture Upscaling

```kotlin
enum class TextureFilter {
    NEAREST,        // Classic pixelated (default)
    LINEAR,         // Bilinear
    ANISOTROPIC_4X, // 4x anisotropic
    ANISOTROPIC_16X // 16x anisotropic (HD mode)
}

fun setTextureFilter(filter: TextureFilter) {
    gl.glBindTexture(GL.GL_TEXTURE_2D, textureAtlasId)
    when (filter) {
        TextureFilter.NEAREST -> {
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST)
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST)
        }
        TextureFilter.ANISOTROPIC_16X -> {
            gl.glTexParameterf(GL.GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY, 16f)
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR)
        }
    }
}
```

## Environment Lighting

```kotlin
data class SceneLighting(
    val ambientColor: FloatArray = floatArrayOf(0.4f, 0.4f, 0.5f),
    val sunDirection: FloatArray = floatArrayOf(-0.577f, -0.577f, -0.577f),
    val sunColor: FloatArray = floatArrayOf(1.0f, 0.95f, 0.8f),
    val shadowDistance: Float = 3000f,
    val shadowMapSize: Int = 4096
)
```

## Runtime Toggle

HD plugin toggles at runtime; software renderer stays ready for fallback:
- `Ctrl+Shift+H` = toggle HD on/off
- Toggle triggers: shader program swap, texture filter change, buffer re-upload
- Target: <100ms toggle time
