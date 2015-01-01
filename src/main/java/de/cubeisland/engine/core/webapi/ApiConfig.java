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

import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;

import de.cubeisland.engine.reflect.Section;
import de.cubeisland.engine.reflect.annotations.Comment;
import de.cubeisland.engine.reflect.codec.yaml.ReflectedYaml;

@SuppressWarnings("all")
public class ApiConfig extends ReflectedYaml
{
    public NetworkSection network;

    public class NetworkSection implements Section
    {
        @Comment("This specifies the address to bind the server to")
        public String address = "localhost";

        @Comment("The port to bind the server to")
        public short port = 6561;

        @Comment("The maximum number of threads for the API server")
        public int maxThreads = 2;

        @Comment("The maxiumum number of concurrent connections from one ip")
        public int maxConnectionPerIp = 2;

        @Comment("The maximum amount of data written from a request")
        public int maxContentLength = 1048576;
    }

    public CompressionSection compression;

    public class CompressionSection implements Section
    {
        @Comment("This enables response compression")
        public boolean enable = false;

        @Comment("The compression level, higher => better compression + more load")
        public int level = 9;

        @Comment("The window bits, higher => better compression + more load")
        public int windowBits = 15;

        @Comment("The memory level, higher => better compression + higher memory usage")
        public int memoryLevel = 9;
    }

    @Comment("This is a list of disabled routes")
    public Set<String> disabledRoutes = Collections.emptySet();

    public BlacklistSection blacklist;

    public class BlacklistSection implements Section
    {
        @Comment("This enables the IP blacklisting")
        public boolean enable = false;

        @Comment("The IPs to block")
        public Set<InetAddress> ips = Collections.emptySet();
    }

    public WhitelistSection whitelist;

    public class WhitelistSection implements Section
    {
        @Comment("This enables the IP whitelisting")
        public boolean enable = false;

        @Comment("The IPs to allow")
        public Set<InetAddress> ips = Collections.emptySet();
    }

    public AuthorizedSection authorizedList;

    public class AuthorizedSection implements Section
    {
        @Comment("This enables IPs to be authorized by default")
        public boolean enable = false;

        @Comment("The IPs to be authorized by default")
        public Set<InetAddress> ips = Collections.emptySet();
    }
}
