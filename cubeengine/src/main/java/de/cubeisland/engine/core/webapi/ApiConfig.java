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
package de.cubeisland.engine.core.webapi;

import java.util.Collections;
import java.util.Set;

import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;

public class ApiConfig extends YamlConfiguration
{
    @Name("network.address")
    @Comment("This specifies the address to bind the server to")
    public String address = "localhost";
    @Name("network.port")
    @Comment("The port to bind the server to")
    public short port = 6561;
    @Name("network.max-threads")
    @Comment("The maximum number of threads for the API server")
    public int maxThreads = 2;
    @Name("network.max-content-length")
    @Comment("The maximum amount of data written from a request")
    public int maxContentLength = 1048576;
    @Name("compression.enable")
    @Comment("This enables response compression")
    public boolean compression = false;
    @Name("compression.level")
    @Comment("The compression level, higher => better compression + more load")
    public int compressionLevel = 9;
    @Name("compression.window-bits")
    @Comment("The window bits, higher => better compression + more load")
    public int windowBits = 15;
    @Name("compression.memory-level")
    @Comment("The memory level, higher => better compression + higher memory usage")
    public int memoryLevel = 9;
    @Name("disabled-routes")
    @Comment("This is a list of disables routes")
    public Set<String> disabledRoutes = Collections.emptySet();
    @Name("blacklist.enable")
    @Comment("This enables the IP blacklisting")
    public boolean blacklistEnable = false;
    @Name("blacklist.ips")
    @Comment("The IPs to block")
    public Set<String> blacklist = Collections.emptySet();
    @Name("whitelist.enable")
    @Comment("This enables the IP whitelisting")
    public boolean whitelistEnable = false;
    @Name("whitelist.ips")
    @Comment("The IPs to allow")
    public Set<String> whitelist = Collections.emptySet();
}
