package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.basics.general.AfkListener;
import de.cubeisland.cubeengine.basics.general.ChatCommands;
import de.cubeisland.cubeengine.basics.general.GeneralsListener;
import de.cubeisland.cubeengine.basics.general.InformationCommands;
import de.cubeisland.cubeengine.basics.general.ListCommand;
import de.cubeisland.cubeengine.basics.mail.MailCommand;
import de.cubeisland.cubeengine.basics.mail.MailManager;
import de.cubeisland.cubeengine.basics.general.MuteListener;
import de.cubeisland.cubeengine.basics.general.PlayerCommands;
import de.cubeisland.cubeengine.basics.moderation.InventoryCommands;
import de.cubeisland.cubeengine.basics.moderation.ItemCommands;
import de.cubeisland.cubeengine.basics.moderation.KickBanCommands;
import de.cubeisland.cubeengine.basics.moderation.KitCommand;
import de.cubeisland.cubeengine.basics.moderation.PowerToolCommand;
import de.cubeisland.cubeengine.basics.moderation.PowerToolListener;
import de.cubeisland.cubeengine.basics.moderation.SpawnMobCommand;
import de.cubeisland.cubeengine.basics.moderation.TimeControlCommands;
import de.cubeisland.cubeengine.basics.moderation.WorldControlCommands;
import de.cubeisland.cubeengine.basics.teleport.MovementCommands;
import de.cubeisland.cubeengine.basics.teleport.SpawnCommands;
import de.cubeisland.cubeengine.basics.teleport.TeleportCommands;
import de.cubeisland.cubeengine.basics.teleport.TeleportListener;
import de.cubeisland.cubeengine.basics.teleport.TeleportRequestCommands;
import de.cubeisland.cubeengine.basics.teleport.TpWorldPermissions;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class Basics extends Module
{
    private BasicsConfiguration config;
    private BasicUserManager basicUM;
    private MailManager mailManager;

    private static Basics instance;

    public static Basics getInstance()
    {
        return instance;
    }

    @Override
    public void onEnable()
    {
        instance = this;
        this.basicUM = new BasicUserManager(this.getDatabase());
        this.mailManager = new MailManager(this.getDatabase(), this.basicUM);
        this.registerPermissions(BasicsPerm.values());

        //General:
        this.registerCommands(new ChatCommands(this));
        this.registerCommands(new InformationCommands(this));
        this.registerCommands(new ListCommand());
        this.registerCommand(new MailCommand(this));
        this.registerCommands(new PlayerCommands(this));
        this.registerListener(new GeneralsListener(this));
        this.registerListener(new MuteListener(this));

        //Moderation:
        this.registerCommands(new InventoryCommands(this));
        this.registerCommands(new ItemCommands(this));
        this.registerCommands(new KickBanCommands());
        this.registerCommands(new SpawnMobCommand(this));
        this.registerCommands(new TimeControlCommands());
        this.registerCommands(new WorldControlCommands(this));
        this.registerCommands(new PowerToolCommand());
        this.registerCommand(new KitCommand(this));
        this.registerListener(new PowerToolListener());

        //Teleport:
        this.registerCommands(new MovementCommands(this));
        this.registerCommands(new SpawnCommands(this));
        this.registerCommands(new TeleportCommands(this));
        this.registerCommands(new TeleportRequestCommands(this));
        this.registerListener(new TeleportListener(this));

        this.registerPermissions(new TpWorldPermissions(this).getPermissions()); // per world permissions
        final long autoAfk;
        final long afkCheck;
        try
        {
            autoAfk = StringUtils.convertTimeToMillis(instance.config.autoAfk);
            afkCheck = StringUtils.convertTimeToMillis(instance.config.afkCheck);
            if (afkCheck < 0)
            {
                throw new IllegalStateException("afk-check-time has to be greater than 0!");
            }
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("illegal time format in configuration!");
        }
        AfkListener afkListener = new AfkListener(this, autoAfk, afkCheck);
        this.registerListener(afkListener);
        if (autoAfk > 0)
        {
            this.getTaskManger().scheduleSyncRepeatingTask(this, afkListener, 20, afkCheck / 50); // this is in ticks so /50
        }

        //TODO register permissions of kits in config

        /**
         * * //commands TODO
         *
         * helpop -> move to CubePermissions ?? not only op but also "Moderator"
         * ignore -> move to CubeChat
         * info
         *
         * nick -> move to CubeChat
         * realname -> move to CubeChat
         * rules
         *
         * help -> Display ALL availiable cmd
         */
    }

    public BasicsConfiguration getConfiguration()
    {
        return this.config;
    }

    public BasicUserManager getBasicUserManager()
    {
        return this.basicUM;
    }

    public MailManager getMailManager()
    {
        return this.mailManager;
    }
}
