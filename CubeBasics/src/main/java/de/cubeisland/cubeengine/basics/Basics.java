package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.basics.general.*;
import de.cubeisland.cubeengine.basics.moderation.*;
import de.cubeisland.cubeengine.basics.teleport.MovementCommands;
import de.cubeisland.cubeengine.basics.teleport.SpawnCommands;
import de.cubeisland.cubeengine.basics.teleport.TeleportCommands;
import de.cubeisland.cubeengine.basics.teleport.TeleportRequestCommands;
import de.cubeisland.cubeengine.basics.teleport.TpWorldPermissions;
import de.cubeisland.cubeengine.core.module.Module;

public class Basics extends Module
{
    private BasicsConfiguration config;
    private BasicUserManager basicUM;
    private MailManager mailManager;
    
    @Override
    public void onEnable()
    {
        this.basicUM = new BasicUserManager(this.getDatabase());
        this.mailManager = new MailManager(this.getDatabase(), this.basicUM);
        this.registerPermissions(BasicsPerm.values());
        //Modules:
        this.registerCommand(new ModuleCommands(this));
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
        
        this.registerListener(new PowerToolListener());
        //Teleport:
        this.registerCommands(new MovementCommands(this));
        this.registerCommands(new SpawnCommands(this));
        this.registerCommands(new TeleportCommands(this));
        this.registerCommands(new TeleportRequestCommands(this));

        this.registerPermissions(new TpWorldPermissions(this).getPermissions()); // per world permissions
        
        
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
