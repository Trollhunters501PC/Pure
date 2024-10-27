package cn.nukkit;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.ServerKiller;

import com.google.common.base.Preconditions;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
@Log4j2
public class Nukkit {

    public final static String NAME = "Pure";
    public final static String VERSION = "1.0.2";
    public final static String API_VERSION = "1.0.15";
    public final static String CODENAME = "PureTeam";
    @Deprecated
    public final static String MINECRAFT_VERSION = ProtocolInfo.MINECRAFT_VERSION;
    @Deprecated
    public final static String MINECRAFT_VERSION_NETWORK = ProtocolInfo.MINECRAFT_VERSION_NETWORK;

    public final static String PATH = System.getProperty("user.dir") + "/";
    public final static String DATA_PATH = System.getProperty("user.dir") + "/";
    public final static String PLUGIN_PATH = DATA_PATH + "plugins";
    public static final long START_TIME = System.currentTimeMillis();
    public static boolean ANSI = true;
    public static boolean TITLE = false;
    public static boolean shortTitle = reqShortTitle();
    public static int DEBUG = 1;

    public static void main(String[] args) {

        System.setProperty("java.net.preferIPv4Stack" , "true");
        System.setProperty("log4j.skipJansi", "false");
        System.getProperties().putIfAbsent("io.netty.allocator.type", "unpooled"); // Disable memory pooling unless specified

        System.setProperty("leveldb.mmap", "true");
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);

        // Define args
        OptionParser parser = new OptionParser();
        OptionSpecBuilder helpSpec = parser.accepts("help", "Shows this page");
        OptionSpecBuilder disableAnsi = parser.accepts("enable-ansi", "Disables console coloring");
        OptionSpecBuilder enableTitle = parser.accepts("enable-title", "Enables title at the top of the window");
        parser.accepts("v", "Set verbosity of logging").withRequiredArg().ofType(String.class);
        parser.accepts("verbosity", "Set verbosity of logging").withRequiredArg().ofType(String.class);

        // Parse arguments
        OptionSet options = parser.parse(args);

        ANSI = options.has(disableAnsi);
        TITLE = options.has(enableTitle);

        Object verbosity = options.valueOf("v");
        if (verbosity == null) {
            verbosity = options.valueOf("-verbosity");
        }
        if (verbosity != null) {

            try {
                Level level = Level.valueOf((String) verbosity);
                setLogLevel(level);
            } catch (Exception e) {
                // ignore
            }
        }

        try {
            if (TITLE) {
                System.out.print((char) 0x1b + "]0;Starting Server For Minecraft: PE" + (char) 0x07);
            }
            new Server(PATH, DATA_PATH, PLUGIN_PATH);
        } catch (Throwable t) {
            log.throwing(t);
        }

        if (TITLE) {
            System.out.print((char) 0x1b + "]0;Stopping Server..." + (char) 0x07);
        }
        log.info("Stopping other threads");

        for (Thread thread : java.lang.Thread.getAllStackTraces().keySet()) {
            if (!(thread instanceof InterruptibleThread)) {
                continue;
            }
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }

        ServerKiller killer = new ServerKiller(8);
        killer.start();

        LogManager.shutdown();

        if (TITLE) {
            System.out.print((char) 0x1b + "]0;Server Stopped" + (char) 0x07);
        }
        System.exit(0);
    }

    private static boolean reqShortTitle() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows") &&(osName.contains("windows 8") || osName.contains("2012"));
    }

    public static void setLogLevel(Level level) {
        Preconditions.checkNotNull(level, "level");
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration log4jConfig = ctx.getConfiguration();
        LoggerConfig loggerConfig = log4jConfig.getLoggerConfig(org.apache.logging.log4j.LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(level);
        ctx.updateLoggers();
    }

    public static Level getLogLevel() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration log4jConfig = ctx.getConfiguration();
        LoggerConfig loggerConfig = log4jConfig.getLoggerConfig(org.apache.logging.log4j.LogManager.ROOT_LOGGER_NAME);
        return loggerConfig.getLevel();
    }

}
