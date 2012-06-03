package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.persistence.Model;
import static de.cubeisland.cubeengine.CubeEngine._;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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

    public void sendMessage(String category, String string, Object... params)
    {
        String translated = _(this, category, string, params);
        Player receiver = this.offlinePlayer.getPlayer();
        if (receiver != null)
        {
            receiver.sendMessage(translated);
        }
    }
}
