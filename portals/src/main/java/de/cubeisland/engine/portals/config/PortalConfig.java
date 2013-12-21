package de.cubeisland.engine.portals.config;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import de.cubeisland.engine.configuration.Section;
import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.core.util.math.BlockVector3;

public class PortalConfig extends YamlConfiguration
{
    public boolean safeTeleport = true;
    public boolean teleportNonPlayers = false;
    public OfflinePlayer owner;
    public World world;

    public PortalRegion location;

    public class PortalRegion implements Section
    {
        public BlockVector3 from;
        public BlockVector3 to;
    }

    public Destination destination;
}
