package net

import engine.TickEngine
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.LoggerFactory
import java.nio.file.Path

/**
 * Netty TCP server on port 43594. Handles both JS5 cache serving and game login
 * on the same port, differentiated by the first byte the client sends (15=JS5,
 * 14=game login handshake).
 *
 * Source: OSRS uses a single port (43594) for both JS5 and game traffic.
 */
class GameServer(
    val port: Int = 43594,
    private val tickEngine: TickEngine,
    private val cacheDir: Path = Path.of("cache"),
) {
    private val log = LoggerFactory.getLogger(GameServer::class.java)

    private val bossGroup   = NioEventLoopGroup(1)
    private val workerGroup = NioEventLoopGroup()
    private val dispatcher  = PacketDispatcher()

    fun dispatcher(): PacketDispatcher = dispatcher

    /**
     * Starts the server and blocks until the channel closes.
     * Call [shutdown] from another thread (or a JVM shutdown hook) to stop it.
     */
    fun start() {
        PacketRegistry.init()

        val bootstrap = ServerBootstrap()
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast("login", LoginHandler(tickEngine, dispatcher, cacheDir))
                }
            })

        val future = bootstrap.bind(port).sync()
        log.info("GameServer listening on port $port")
        future.channel().closeFuture().sync()
    }

    fun shutdown() {
        log.info("GameServer shutting down")
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}
