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
package de.cubeisland.engine.portals.config;

import org.bukkit.OfflinePlayer;

import de.cubeisland.engine.configuration.Section;
import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.core.world.ConfigWorld;

public class PortalConfig extends YamlConfiguration
{
    public boolean safeTeleport = false;
    public boolean teleportNonPlayers = false;
    public OfflinePlayer owner;
    public ConfigWorld world;

    public final PortalRegion location = new PortalRegion();

    public class PortalRegion implements Section
    {
        public BlockVector3 from;
        public BlockVector3 to;

        @Comment("When linking another portal to this one a player will be teleported to this location")
        public WorldLocation destination;
    }

    public Destination destination;
}
