package de.cubeisland.cubeengine.irc;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a IrcConfig
 */
@Codec("yml")
@DefaultConfig
public class IrcConfig extends Configuration
{
    @Option("server.test")
    public String host = "irc.esper.net";

    @Option("server.port")
    public int port = 6667;

    @Option("server.ssl")
    public boolean ssl = true;

    @Option("server.trust-all-certificates")
    public boolean trustAllCerts = false;

    @Option("server.password")
    public String password = "";

    @Option("server.channels")
    public Set<String> channels = Collections.emptySet();

    @Option("bot.name")
    public String botName = "CubeBot";

    @Option("bot.bot-per-user")
    @Comment("Whether to use a bot per user (most networks will not allow this)")
    public boolean botPerUser = false;

    @Option("bot.auto-reconnect")
    public boolean autoReconnect = true;
}