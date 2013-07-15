/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.irc;

import java.util.Collections;
import java.util.Set;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;

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
