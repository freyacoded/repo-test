package lavalink.server.config

import moe.kyokobot.koe.KoeOptions
import moe.kyokobot.koe.codec.udpqueue.UdpQueueFramePollerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KoeConfiguration(val serverConfig: ServerConfig) {

    private val log: Logger = LoggerFactory.getLogger(KoeConfiguration::class.java)

    @Bean
    fun koeOptions(): KoeOptions = KoeOptions.builder().apply {
        log.info("OS: " + System.getProperty("os.name") + ", Arch: " + System.getProperty("os.arch"))
        val os = System.getProperty("os.name")
        val arch = System.getProperty("os.arch")

        // Maybe add Windows natives back?
        val nasSupported = os.contains("linux", ignoreCase = true)
                && arch.equals("amd64", ignoreCase = true)

        if (nasSupported) {
            log.info("Enabling JDA-NAS")
            setFramePollerFactory(UdpQueueFramePollerFactory())
        } else {
            log.warn("This system and architecture appears to not support native audio sending! "
                    + "GC pauses may cause your bot to stutter during playback.")
        }
    }.create()
}