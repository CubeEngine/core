package de.cubeisland.cubeengine.core.user;

import static de.cubeisland.cubeengine.CubeEngine._;
import de.cubeisland.cubeengine.core.persistence.Model;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Phillip Schichtel
 */
public class User extends UserBase implements Model<Integer>
{
    private final OfflinePlayer player;
    private int key;
    private String language;
    public static final int BLOCK_FLY = 1;

    public User(int key, OfflinePlayer player, String language)
    {
        super(player);
        this.key = key;
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

    public Integer getKey()
    {
        return this.key;
    }

    public void setKey(Integer key)
    {
        this.key = key;
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
        String category = className.substring(25, className.indexOf(".", 26));
        String translated = _(this, category, string, params);
        this.sendMessage(translated);
    }
}
