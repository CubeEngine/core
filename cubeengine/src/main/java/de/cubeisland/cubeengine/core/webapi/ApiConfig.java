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
package de.cubeisland.cubeengine.core.webapi;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.annotations.Revision;
import java.util.Collections;
import java.util.Set;

@Codec("yml")
@Revision(1)
public class ApiConfig extends Configuration
{
    @Option("network.address")
    @Comment("This specifies the address to bind the server to")
    public String address = "localhost";
    @Option("network.port")
    @Comment("The port to bind the server to")
    public short port = 6561;
    @Option("network.max-threads")
    @Comment("The maximum number of threads for the API server")
    public int maxThreads = 2;
    @Option("network.max-content-length")
    @Comment("The maximum amount of data written from a request")
    public int maxContentLength = 1048576;
    @Option("compression.enable")
    @Comment("This enables response compression")
    public boolean compression = false;
    @Option("compression.level")
    @Comment("The compression level, higher => better compression + more load")
    public int compressionLevel = 9;
    @Option("compression.window-bits")
    @Comment("The window bits, higher => better compression + more load")
    public int windowBits = 15;
    @Option("compression.memory-level")
    @Comment("The memory level, higher => better compression + higher memory usage")
    public int memoryLevel = 9;
    @Option("disabled-routes")
    @Comment("This is a list of disables routes")
    public Set<String> disabledRoutes = Collections.emptySet();
    @Option("blacklist.enable")
    @Comment("This enables the IP blacklisting")
    public boolean blacklistEnable = false;
    @Option("blacklist.ips")
    @Comment("The IPs to block")
    public Set<String> blacklist = Collections.emptySet();
    @Option("whitelist.enable")
    @Comment("This enables the IP whitelisting")
    public boolean whitelistEnable = false;
    @Option("whitelist.ips")
    @Comment("The IPs to allow")
    public Set<String> whitelist = Collections.emptySet();
}
