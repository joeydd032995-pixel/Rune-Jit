package net

import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream

object PacketRegistry {
    private val log = LoggerFactory.getLogger(PacketRegistry::class.java)

    // opcode to packet name (C2S); -1 = unknown size
    private var opcodeToName: Map<Int, String> = emptyMap()
    // packet name to fixed size (-1 = VAR_BYTE, -2 = VAR_SHORT, N = fixed)
    private var nameToSize:   Map<String, Int>  = emptyMap()

    fun init(defsPath: Path = Path.of("src/shared/protocol-defs.yaml")) {
        if (!defsPath.exists()) {
            log.warn("protocol-defs.yaml not found at $defsPath -- packet decoding disabled until Agent 3 creates it")
            return
        }
        @Suppress("UNCHECKED_CAST")
        val root = Yaml().load<Map<String, Any>>(defsPath.inputStream())
        val c2s = root["client_to_server"] as? List<Map<String, Any>> ?: emptyList()
        val byOpcode = mutableMapOf<Int, String>()
        val byName   = mutableMapOf<String, Int>()
        for (entry in c2s) {
            val name   = entry["name"] as? String ?: continue
            val opcode = (entry["opcode"] as? Int)   // null if not yet confirmed
            val size   = (entry["size"] as? Int) ?: -1
            byName[name] = size
            if (opcode != null) byOpcode[opcode] = name
        }
        opcodeToName = byOpcode
        nameToSize   = byName
        log.info("PacketRegistry loaded ${byOpcode.size} C2S opcodes, ${byName.size} packet definitions")
    }

    fun nameFor(opcode: Int): String? = opcodeToName[opcode]
    fun sizeFor(name: String): Int    = nameToSize[name] ?: -1
    fun isInitialized(): Boolean      = nameToSize.isNotEmpty()
}
