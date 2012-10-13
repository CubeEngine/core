package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import de.cubeisland.cubeengine.core.storage.LinkingModel;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author Phillip Schichtel
 */
@Entity(name = "user")
public class User extends UserBase implements LinkingModel<Integer>
{
    public static int NO_ID = -1;
    @Key
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int key;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public final OfflinePlayer player;
    @Attribute(type = AttrType.BOOLEAN)
    public boolean nogc = false;
    @Attribute(type = AttrType.DATETIME)
    public Timestamp lastseen;
    private ConcurrentHashMap<Class<? extends Model>, Model> attachments;
    private ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    @DatabaseConstructor
    public User(List<Object> args) throws ConversionException
    {
        super(CubeEngine.getOfflinePlayer((String)args.get(1)));
        this.key = Convert.fromObject(Integer.class, args.get(0));
        this.player = this.offlinePlayer;
        this.nogc = (Boolean)args.get(2);
        this.lastseen = (Timestamp)args.get(3);
    }

    public User(int key, OfflinePlayer player)
    {
        super(player);
        this.key = key;
        this.player = player;
        this.lastseen = new Timestamp(System.currentTimeMillis());
    }

    public User(OfflinePlayer player)
    {
        this(NO_ID, player);
    }

    public User(String playername)
    {
        this(NO_ID, CubeEngine.getOfflinePlayer(playername));
    }

    /**
     * @return the offlineplayer
     */
    public OfflinePlayer getOfflinePlayer()
    {
        return this.player;
    }

    @Override
    public Integer getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Integer id)
    {
        this.key = id;
    }

    @Override
    public void sendMessage(String string)
    {
        if (!Thread.currentThread().getStackTrace()[1].getClassName().equals(this.getClass().getName()))
        {
            if (CubeEngine.getCore().isDebug())
            {
                CubeEngine.getLogger().warning("A module sent an untranslated message!");
            }
        }
        super.sendMessage(string);
    }

    /**
     * Sends a translated Message to this User
     *
     * @param string the message to translate
     * @param params optional parameter
     */
    public void sendMessage(String category, String string, Object... params)
    {
        this.sendMessage(_(this, category, string, params));
    }

    @Override
    public <T extends Model> void attach(T model)
    {
        if (this.attachments == null)
        {
            this.attachments = new ConcurrentHashMap<Class<? extends Model>, Model>();
        }
        this.attachments.put(model.getClass(), model);
    }

    @Override
    public <T extends Model> T getAttachment(Class<T> modelClass)
    {
        if (this.attachments == null)
        {
            return null;
        }
        return (T)this.attachments.get(modelClass);
    }

    public String getLanguage()
    {
        String language = null;
        Player onlinePlayer = this.offlinePlayer.getPlayer();
        if (onlinePlayer != null)
        {
            language = BukkitUtils.getLanguage(onlinePlayer);
        }
        if (language == null)
        {
            language = CubeEngine.getCore().getConfiguration().defaultLanguage;
        }
        return language;
    }

    @Override
    public long getLastPlayed()
    {
        if (this.isOnline())
        {
            return 0;
        }
        return this.lastseen.getTime();
    }

    public void setAttribute(String name, Object value)
    {
        Validate.notNull(name, "The attribute name must not be null!");
        Validate.notNull(value, "Null-values are not allowed!");

        this.attributes.put(name, value);
    }

    public <T extends Object> T getAttribute(String name)
    {
        return this.<T>getAttribute(name, null);
    }

    public <T extends Object> T getAttribute(String name, T def)
    {
        try
        {
            T value = (T)this.attributes.get(name);
            if (value != null)
            {
                return value;
            }
        }
        catch (ClassCastException ignored)
        {
        }
        return def;
    }

    public void removeAttribute(String name)
    {
        this.attributes.remove(name);
    }
}