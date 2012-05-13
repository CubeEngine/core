package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.libMinecraft.bitmask.LongBitMask;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author Phillip Schichtel
 */
public class CubeUser implements Model
{
    private final OfflinePlayer player;
    private final LongBitMask flags;
    private final int id;
    
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
     * Returns the player or null if not online
     *
     * @return the player
     */
    public Player getPlayer()
    {
        return this.player.getPlayer();
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
    
    public boolean isOnline()
    {
        return player.isOnline();
    }
}
