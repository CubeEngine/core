package de.cubeisland.cubeengine.core.user;

import static de.cubeisland.cubeengine.CubeEngine._;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Phillip Schichtel
 */
@Entity(name = "user")
public class User extends UserBase
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true)
    private int id;
    
    @Attribute(type = AttrType.VARCHAR, length=16)
    private final OfflinePlayer player;
    
    @Attribute(type = AttrType.VARCHAR, length=5)
    private String language;
    
    
    public static final int BLOCK_FLY = 1;

    public User(int key, OfflinePlayer player, String language)
    {
        super(player);
        this.id = key;
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

    public void setLanguage(String lang)
    {
        this.language = lang;
    }

    public String getLanguage()
    {
        return this.language;
    }

    public Integer getId()
    {
        return this.id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * Sends a translated Message to this User
     *
     * @param string the message to translate
     * @param params optional parameter
     */
    public void sendTMessage(String string, Object... params)
    {
        final String className = Thread.currentThread().getStackTrace()[2].getClassName();
        String category = className.substring(25, className.indexOf('.', 26));
        String translated = _(this, category, string, params);
        this.sendMessage(translated);
    }
}