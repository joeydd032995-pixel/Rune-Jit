---
name: gamepack-loader
description: "Handles loading the obfuscated Jagex gamepack JAR, applies reverse-engineered field/method mappings, injects hooks for private server connectivity, and patches the login screen to connect to a custom server."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Gamepack Loader

You implement the pipeline for loading and patching the OSRS gamepack for private server use.

## Overview

The OSRS client is an obfuscated Java applet (gamepack). For private server use:
1. Download exact revision gamepack JAR
2. Apply reverse-engineered class/field/method mappings
3. Inject hooks into game loops
4. Patch login screen to accept custom server address
5. Patch session authentication (disable Jagex auth for private use)

## Mappings Format

Mappings produced by `reverse-engineer` agent stored at `src/shared/mappings.yaml`:

```yaml
revision: 225
classes:
  client:
    obfuscated: "ea"
    fields:
      gameCycle: { obfuscated: "aj", type: "int", multiplier: 1299814923 }
      localPlayer: { obfuscated: "bq", type: "Player" }
      npcCount: { obfuscated: "ch", type: "int", multiplier: 1023442291 }
  player:
    obfuscated: "hf"
    fields:
      name: { obfuscated: "az", type: "String" }
      combatLevel: { obfuscated: "bn", type: "int", multiplier: 1788927093 }
methods:
  client:
    drawWidgets: { obfuscated: "bp", descriptor: "(I)V" }
    gameLoop: { obfuscated: "ar", descriptor: "()V" }
```

## Hook Injection

Use ASM bytecode manipulation to inject hooks at key points:

```kotlin
class GameLoopHookInjector : ClassVisitor(ASM9) {
    override fun visitMethod(access: Int, name: String, descriptor: String, ...): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, ...)
        return if (name == mappings.gameLoopMethod) {
            GameLoopMethodVisitor(mv)
        } else mv
    }
}

class GameLoopMethodVisitor(mv: MethodVisitor) : MethodVisitor(ASM9, mv) {
    override fun visitInsn(opcode: Int) {
        if (opcode == RETURN) {
            // Inject: Hooks.onGameTick()
            mv.visitMethodInsn(INVOKESTATIC, "Hooks", "onGameTick", "()V", false)
        }
        super.visitInsn(opcode)
    }
}
```

## Login Screen Patch

Patch the hardcoded server address in the gamepack:

```kotlin
class LoginPatchTransformer : ClassFileTransformer {
    val targetServer = System.getProperty("osrs.server", "localhost")
    val targetPort = System.getProperty("osrs.port", "43594").toInt()

    override fun transform(loader: ClassLoader?, className: String, ...) {
        if (className != mappings.loginScreenClass) return null
        val cr = ClassReader(classfileBuffer)
        val cw = ClassWriter(cr, ClassWriter.COMPUTE_FRAMES)
        cr.accept(LoginPatchVisitor(cw, targetServer, targetPort), 0)
        return cw.toByteArray()
    }
}
```

## Jagex Auth Bypass (Private Use Only)

**IMPORTANT**: This patch is for private/educational servers only. Never use against official servers.

```kotlin
class AuthBypassTransformer : ClassFileTransformer {
    override fun transform(loader: ClassLoader?, className: String, ...) {
        if (className != mappings.authClass) return null
        // Replace checkToken() with a stub that always returns true
        val cr = ClassReader(classfileBuffer)
        val cw = ClassWriter(cr, 0)
        cr.accept(AuthBypassVisitor(cw), 0)
        return cw.toByteArray()
    }
}
```

## Class Loader

```kotlin
class PatchedGamepackLoader(val gamepPackPath: Path) : URLClassLoader(arrayOf(gamepPackPath.toUri().toURL())) {
    val transformers = listOf(LoginPatchTransformer(), AuthBypassTransformer(), HookInjector())

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        val bytes = findClass(name)?.let { transform(it) }
            ?: return super.loadClass(name, resolve)
        return defineClass(name, bytes, 0, bytes.size)
    }
}
```

## Security Notes

- Gamepack JAR is stored in `gamepacks/` (gitignored)
- Mappings YAML can be committed; they contain no Jagex IP
- Auth bypass only disables server-side auth check; still authenticates to private server
