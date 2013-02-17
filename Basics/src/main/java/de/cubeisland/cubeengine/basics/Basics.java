package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.basics.command.general.*;
import de.cubeisland.cubeengine.basics.command.mail.MailCommand;
import de.cubeisland.cubeengine.basics.command.mail.MailManager;
import de.cubeisland.cubeengine.basics.command.moderation.*;
import de.cubeisland.cubeengine.basics.command.moderation.kit.*;
import de.cubeisland.cubeengine.basics.command.moderation.spawnmob.SpawnMobCommand;
import de.cubeisland.cubeengine.basics.command.teleport.*;
import de.cubeisland.cubeengine.basics.storage.BasicUserManager;
import de.cubeisland.cubeengine.basics.storage.IgnoreListManager;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;

public class Basics extends Module
{

    private BasicsConfiguration config;
    private BasicUserManager basicUM;
    private MailManager mailManager;
    private KitsGivenManager kitGivenManager;
    private IgnoreListManager ignoreListManager;
    public int afkListenerTask;
    private KitManager kitManager;
    private LagTimer lagTimer;

    @Override
    public void onEnable()
    {
        this.basicUM = new BasicUserManager(this.getDatabase());
        this.mailManager = new MailManager(this.getDatabase(), this.basicUM);
        this.ignoreListManager = new IgnoreListManager(this.getDatabase());
        this.registerPermissions(BasicsPerm.values());

        this.registerListener(new ColoredSigns());

        //General:
        this.registerCommands(new ChatCommands(this), ReflectedCommand.class);
        this.registerCommands(new InformationCommands(this), ReflectedCommand.class);
        this.registerCommands(new ListCommand(this), ReflectedCommand.class);
        this.registerCommand(new MailCommand(this));
        this.registerCommands(new PlayerCommands(this), ReflectedCommand.class);
        this.registerListener(new GeneralsListener(this));
        this.registerListener(new MuteListener(this));

        //Moderation:
        this.registerCommands(new InventoryCommands(this), ReflectedCommand.class);
        this.registerCommands(new ItemCommands(this), ReflectedCommand.class);
        this.registerCommands(new KickBanCommands(this), ReflectedCommand.class);
        this.registerCommands(new SpawnMobCommand(this), ReflectedCommand.class);
        this.registerCommands(new TimeControlCommands(this), ReflectedCommand.class);
        this.registerCommands(new WorldControlCommands(this), ReflectedCommand.class);
        PowerToolCommand ptCommands = new PowerToolCommand(this);
        this.registerCommand(ptCommands);
        this.registerListener(ptCommands);
        this.registerCommand(new KitCommand(this));

        Convert.registerConverter(KitItem.class, new KitItemConverter());

        this.kitManager = new KitManager(this);
        kitManager.loadKits();
        this.kitGivenManager = new KitsGivenManager(this.getDatabase());

        //Teleport:
        this.registerCommands(new MovementCommands(this), ReflectedCommand.class);
        this.registerCommands(new SpawnCommands(this), ReflectedCommand.class);
        this.registerCommands(new TeleportCommands(this), ReflectedCommand.class);
        this.registerCommands(new TeleportRequestCommands(this), ReflectedCommand.class);
        this.registerListener(new TeleportListener(this));

        this.registerPermissions(new TpWorldPermissions(this).getPermissions()); // per world permissions
        final long autoAfk;
        final long afkCheck;
        try
        {
            autoAfk = StringUtils.convertTimeToMillis(Basics.this.config.autoAfk);
            afkCheck = StringUtils.convertTimeToMillis(Basics.this.config.afkCheck);
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
            this.afkListenerTask = this.getTaskManger().scheduleSyncRepeatingTask(this, afkListener, 20, afkCheck / 50); // this is in ticks so /50
        }
        this.lagTimer = new LagTimer(this);

        /**
         * * //commands TODO
         *
         * helpop -> move to CubePermissions ?? not only op but also "Moderator"
         * ignore -> move to CubeChat info
         *
         * nick -> move to CubeChat realname -> move to CubeChat rules
         *
         * help -> Display ALL availiable cmd
         */
    }

    @Override
    public void onDisable()
    {
        this.getTaskManger().cancelTask(this, this.afkListenerTask);
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

    public KitsGivenManager getKitGivenManager()
    {
        return kitGivenManager;
    }

    public IgnoreListManager getIgnoreListManager()
    {
        return ignoreListManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public LagTimer getLagTimer() {
        return lagTimer;
    }
}
