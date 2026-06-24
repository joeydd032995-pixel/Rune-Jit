package net

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Serves OSRS cache files over the JS5 protocol.
 *
 * JS5 request: [priority:1][archive:1][file:2]  (4 bytes per request)
 *   priority 0 = low, 1 = high
 * JS5 response: [archive:1][file:2][compression:1][size:4][data:size]
 *
 * If the cache directory is absent (not yet downloaded), all requests receive
 * an empty-data response so the client does not hang.
 *
 * Source: OSRS JS5 protocol, https://github.com/RuneStar/cs2
 */
class JS5Handler(private val cacheDir: Path) : ChannelInboundHandlerAdapter() {
    private val log = LoggerFactory.getLogger(JS5Handler::class.java)

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is ByteBuf) return
        try {
            while (msg.readableBytes() >= 4) {
                val priority = msg.readUnsignedByte().toInt()
                val archive  = msg.readUnsignedByte().toInt()
                val file     = msg.readUnsignedShort()
                serveFile(ctx, archive, file)
            }
        } finally {
            msg.release()
        }
    }

    private fun serveFile(ctx: ChannelHandlerContext, archive: Int, file: Int) {
        val cachePresent = cacheDir.exists()
        val fileData: ByteArray = if (cachePresent) {
            readCacheFile(archive, file)
        } else {
            log.debug("Cache not present — sending empty response for archive=$archive file=$file")
            ByteArray(0)
        }

        // Response: [archive:1][file:2][compression:1][dataSize:4][data:N]
        val buf = ctx.alloc().buffer(8 + fileData.size)
        buf.writeByte(archive)
        buf.writeShort(file)
        buf.writeByte(0)              // compression = none
        buf.writeInt(fileData.size)
        buf.writeBytes(fileData)
        ctx.writeAndFlush(buf)
    }

    private fun readCacheFile(archive: Int, file: Int): ByteArray {
        // Standard OSRS cache layout: main_file_cache.dat2 + main_file_cache.idx{archive}
        val idxPath = cacheDir.resolve("main_file_cache.idx$archive")
        val datPath = cacheDir.resolve("main_file_cache.dat2")
        if (!idxPath.exists() || !datPath.exists()) return ByteArray(0)
        return try {
            idxPath.toFile().inputStream().use { idx ->
                val offset = file.toLong() * 6
                idx.skip(offset)
                val header = ByteArray(6)
                if (idx.read(header) < 6) return ByteArray(0)
                val size   = (header[0].toInt() and 0xFF shl 16) or
                             (header[1].toInt() and 0xFF shl 8) or
                             (header[2].toInt() and 0xFF)
                val sector = (header[3].toInt() and 0xFF shl 16) or
                             (header[4].toInt() and 0xFF shl 8) or
                             (header[5].toInt() and 0xFF)
                if (size == 0 || sector == 0) return ByteArray(0)
                datPath.toFile().inputStream().use { dat ->
                    dat.skip(sector.toLong() * 520L)
                    val data = ByteArray(size)
                    dat.read(data)
                    data
                }
            }
        } catch (e: Exception) {
            log.warn("Failed to read cache archive=$archive file=$file: ${e.message}")
            ByteArray(0)
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.warn("JS5 error: ${cause.message}")
        ctx.close()
    }
}
