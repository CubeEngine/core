package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.BukkitDependend;
import de.cubeisland.cubeengine.core.CubeEngine;
import static de.cubeisland.cubeengine.core.CubeEngine._;
import de.cubeisland.cubeengine.core.storage.LinkingModel;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import java.lang.reflect.Field;
import java.util.List;
import java.util.WeakHashMap;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Phillip Schichtel
 */
@Entity(name = "user")
public class User extends UserBase implements LinkingModel<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int key;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public final OfflinePlayer player;
    
    private WeakHashMap<Class<? extends Model>, Model> attachments;

    @DatabaseConstructor
    public User(List<Object> args)
    {
        super(CubeEngine.getOfflinePlayer((String)args.get(1)));
        try
        {
            this.key = Convert.fromObject(Integer.class, args.get(0));
            this.player = this.offlinePlayer;
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("Error while creating a User from Database");
        }
    }

    public User(int key, OfflinePlayer player)
    {
        super(player);
        this.key = key;
        this.player = player;
    }

    public User(OfflinePlayer player)
    {
        this(-1, player);
    }

    @BukkitDependend("Uses the OfflinePlayer")
    public User(String playername)
    {
        this(-1, CubeEngine.getOfflinePlayer(playername));
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
        // TODO this should be removed before a release or disabled via some kind of debug flag!
        if (!Thread.currentThread().getStackTrace()[1].getClassName().equals(this.getClass().getName()))
        {
            CubeEngine.getLogger().warning("A module sent an untranslated message!");
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
        this.attachments.put(model.getClass(), model);
    }

    @Override
    public <T extends Model> T getAttachment(Class<T> modelClass)
    {
        return (T)this.attachments.get(modelClass);
    }
    
    private static boolean languageWorkaroundInitialized;
    private static Field localeStringField;
    private static Field handleField;
    private static Field localeField;
    
    static
    {
        try
        {
            localeStringField = Class.forName("net.minecraft.server.LocaleLanguage").getDeclaredField("d");
            localeStringField.setAccessible(true);
            
            handleField = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer").getDeclaredField("entity");
            handleField.setAccessible(true);
            
            localeField = Class.forName("net.minecraft.server.EntityPlayer").getDeclaredField("locale");
            localeField.setAccessible(true);
            
            languageWorkaroundInitialized = true;
        }
        catch (Exception e)
        {
            languageWorkaroundInitialized = false;
        }
    }
    
    public String getLanguage()
    {
        if (languageWorkaroundInitialized)
        {
            try
            {
                return (String)localeStringField.get(localeField.get(handleField.get(this)));
            }
            catch (Exception e)
            {}
        }
        return CubeEngine.getCore().getConfiguration().defaultLanguage;   
    }
}