package de.cubeisland.cubeengine.core.user;

import de.cubeisland.libMinecraft.bitmask.LongBitMask;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Phillip Schichtel
 */
public class CubeUser
{
    private final OfflinePlayer player;
    private LongBitMask flags;

    public CubeUser(OfflinePlayer player, LongBitMask flags)
    {
        this.player = player;
        this.flags = flags;
    }

    /**
     * @return the player
     */
    public OfflinePlayer getPlayer()
    {
        return this.player;
    }

    /**
     * @return the flags
     */
    public LongBitMask getFlags()
    {
        return this.flags;
    }
}
