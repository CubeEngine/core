package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.cubeengine.core.util.bitmask.LongBitMask;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Phillip Schichtel
 */
public class User extends UserBase implements Model
{
    private final OfflinePlayer player;
    private int id;
    private String language;
    
    
    public static final int BLOCK_FLY = 1;
    
    
    public User(int id, OfflinePlayer player, String language)
    {
        super(player);
        this.id = id;
        this.player = player;
        this.language = language;
    }
    
    public User(OfflinePlayer player)
    {
        this(-1, player, "en"); //TODO locate user and lookup language ?
    }

    /**
     * @return the offlineplayer
     */
    public OfflinePlayer getOfflinePlayer()
    {
        return this.player;
    }

    /**
     * @return the CubeUsers ID
     */
    public int getId()
    {
        return this.id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(int id)
    {
        this.id = id;
    }
    
    public void setLanguage(String lang)
    {
        this.language = lang;
    }
    
    public String getLanguage()
    {
        return this.language;
    }
}
