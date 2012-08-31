package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.BukkitDependend;
import de.cubeisland.cubeengine.CubeEngine;
import static de.cubeisland.cubeengine.CubeEngine._;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Phillip Schichtel
 */
@Entity(name = "user")
public class User extends UserBase implements Model<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int key;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public final OfflinePlayer player;
    @Attribute(type = AttrType.VARCHAR, length = 5)
    public String language;
    public static final int BLOCK_FLY = 1;

    @DatabaseConstructor
    public User(List<Object> args)
    {
        super((String)args.get(1));
        try
        {
            this.key = Convert.fromObject(Integer.class, args.get(0));
            this.player = this.offlinePlayer;
            this.language = (String)args.get(2);
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("Error while creating a User from Database");
        }
    }

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

    @BukkitDependend("Uses the OfflinePlayer")
    public User(String playername)
    {
        this(-1, CubeEngine.getOfflinePlayer(playername), "en"); //TODO locate user and lookup language ?
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

    public void setKey(Integer id)
    {
        this.key = id;
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