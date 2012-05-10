package de.cubeisland.cubeengine.core.user;

import de.cubeisland.libMinecraft.bitmask.LongBitMask;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author Phillip Schichtel
 */
public class CubeUser
{
    private final OfflinePlayer player;
    private LongBitMask flags;
    private final int id;//TODO
    
    public CubeUser(int id, OfflinePlayer player, LongBitMask flags)
    {
        this.id = id;
        this.player = player;
        this.flags = flags;
    }

    /**
     * @return the offlineplayer
     */
    public OfflinePlayer getOfflinePlayer()
    {
        return this.player;
    }
    
    /**
     * @return the player if online ; else null
     */
    public Player getPlayer()
    {
        if (this.player.isOnline())
            return (Player)this.player;
        else
            return null;
    }  
    
     /**
     * @return the players name
     */
    public String getName()
    {
        return this.player.getName();
    }  

    /**
     * @return the flags
     */
    public LongBitMask getFlags()
    {
        return this.flags;
    }

    /**
     * @return the CubeUsers ID
     */
    public int getId()
    {
        return this.id;
    }
}
