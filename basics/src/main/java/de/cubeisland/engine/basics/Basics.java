/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.basics;

import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;

import de.cubeisland.engine.basics.command.general.ChatCommands;
import de.cubeisland.engine.basics.command.general.ColoredSigns;
import de.cubeisland.engine.basics.command.general.FlyListener;
import de.cubeisland.engine.basics.command.general.GeneralsListener;
import de.cubeisland.engine.basics.command.general.IgnoreCommands;
import de.cubeisland.engine.basics.command.general.InformationCommands;
import de.cubeisland.engine.basics.command.general.LagTimer;
import de.cubeisland.engine.basics.command.general.ListCommand;
import de.cubeisland.engine.basics.command.general.MailCommand;
import de.cubeisland.engine.basics.command.general.MuteListener;
import de.cubeisland.engine.basics.command.general.PlayerCommands;
import de.cubeisland.engine.basics.command.general.RolesListCommand;
import de.cubeisland.engine.basics.command.moderation.DoorCommand;
import de.cubeisland.engine.basics.command.moderation.InventoryCommands;
import de.cubeisland.engine.basics.command.moderation.ItemCommands;
import de.cubeisland.engine.basics.command.moderation.KickBanCommands;
import de.cubeisland.engine.basics.command.moderation.PaintingListener;
import de.cubeisland.engine.basics.command.moderation.PowerToolCommand;
import de.cubeisland.engine.basics.command.moderation.TimeControlCommands;
import de.cubeisland.engine.basics.command.moderation.WorldControlCommands;
import de.cubeisland.engine.basics.command.moderation.kit.KitCommand;
import de.cubeisland.engine.basics.command.moderation.kit.KitItem;
import de.cubeisland.engine.basics.command.moderation.kit.KitItemConverter;
import de.cubeisland.engine.basics.command.moderation.kit.KitManager;
import de.cubeisland.engine.basics.command.moderation.kit.TableKitsGiven;
import de.cubeisland.engine.basics.command.moderation.spawnmob.SpawnMobCommand;
import de.cubeisland.engine.basics.command.teleport.MovementCommands;
import de.cubeisland.engine.basics.command.teleport.SpawnCommands;
import de.cubeisland.engine.basics.command.teleport.TeleportCommands;
import de.cubeisland.engine.basics.command.teleport.TeleportListener;
import de.cubeisland.engine.basics.command.teleport.TeleportRequestCommands;
import de.cubeisland.engine.basics.storage.TableBasicsUser;
import de.cubeisland.engine.basics.storage.TableIgnorelist;
import de.cubeisland.engine.basics.storage.TableMail;
import de.cubeisland.engine.core.bukkit.EventManager;
import de.cubeisland.engine.core.command.CommandManager;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.roles.Roles;

public class Basics extends Module
{
    private BasicsConfiguration config;
    private KitManager kitManager;
    private LagTimer lagTimer;

    @Override
    public void onEnable()
    {
        Profiler.startProfiling("basicsEnable");

        this.config = this.loadConfig(BasicsConfiguration.class);
		final Database db = this.getCore().getDB();
        db.registerTable(TableBasicsUser.class);
        db.registerTable(TableIgnorelist.class);
        db.registerTable(TableMail.class);
        db.registerTable(TableKitsGiven.class);
        final CommandManager cm = this.getCore().getCommandManager();
        final EventManager em = this.getCore().getEventManager();
        this.getLog().trace("{} ms - Basics.Permission", Profiler.getCurrentDelta("basicsEnable", TimeUnit.MILLISECONDS));
        new BasicsPerm(this);
        this.getCore().getUserManager().addDefaultAttachment(BasicsAttachment.class, this);

        em.registerListener(this, new ColoredSigns());

        this.getLog().trace("{} ms - General-Commands", Profiler.getCurrentDelta("basicsEnable", TimeUnit.MILLISECONDS));
        //General:
        IgnoreCommands ignoreCommands = new IgnoreCommands(this);
        cm.registerCommands(this, ignoreCommands , ReflectedCommand.class);
        cm.registerCommands(this, new ChatCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new InformationCommands(this), ReflectedCommand.class);
        cm.registerCommand(new MailCommand(this));
        cm.registerCommands(this, new PlayerCommands(this), ReflectedCommand.class);
        this.getLog().trace("{} ms - General-Listener", Profiler.getCurrentDelta("basicsEnable", TimeUnit.MILLISECONDS));
        em.registerListener(this, new GeneralsListener(this));
        em.registerListener(this, new MuteListener(this, ignoreCommands));
        this.getLog().trace("{} ms - Moderation-Commands", Profiler.getCurrentDelta("basicsEnable", TimeUnit.MILLISECONDS));
        //Moderation:
        cm.registerCommands(this, new InventoryCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new ItemCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new KickBanCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new SpawnMobCommand(this), ReflectedCommand.class);
        cm.registerCommands(this, new TimeControlCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new WorldControlCommands(this), ReflectedCommand.class);
        PowerToolCommand ptCommands = new PowerToolCommand(this);
        cm.registerCommand(ptCommands);
        em.registerListener(this, ptCommands);
        cm.registerCommand(new KitCommand(this));

        Module roles = getCore().getModuleManager().getModule("roles");
        if (roles != null && roles instanceof Roles)
        {
            cm.registerCommand(new RolesListCommand(this));
        }
        else
        {
            this.getLog().info("No Roles-Module found!");
            cm.registerCommand(new ListCommand(this));
        }
        
        em.registerListener(this, new PaintingListener(this));

        this.getLog().trace("{} ms - Kits", Profiler.getCurrentDelta("basicsEnable", TimeUnit.MILLISECONDS));
        this.getCore().getConfigFactory().getDefaultConverterManager().
            registerConverter(KitItem.class, new KitItemConverter());

        this.kitManager = new KitManager(this);
        this.kitManager.loadKits();
        this.getLog().trace("{} ms - Teleport-Commands", Profiler.getCurrentDelta("basicsEnable", TimeUnit.MILLISECONDS));
        //Teleport:
        cm.registerCommands(this, new MovementCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new SpawnCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new TeleportCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new TeleportRequestCommands(this), ReflectedCommand.class);
        this.getLog().trace("{} ms - Teleport/Fly-Listener", Profiler.getCurrentDelta("basicsEnable", TimeUnit.MILLISECONDS));
        em.registerListener(this, new TeleportListener(this));
        em.registerListener(this, new FlyListener());

        this.lagTimer = new LagTimer(this);

        cm.registerCommands(this,  new DoorCommand(this), ReflectedCommand.class );

        this.getLog().trace("{} ms - done", Profiler.endProfiling("basicsEnable", TimeUnit.MILLISECONDS));
    }

    public BasicsConfiguration getConfiguration()
    {
        return this.config;
    }

    public KitManager getKitManager() {
        return this.kitManager;
    }

    public LagTimer getLagTimer() {
        return this.lagTimer;
    }

    public BasicsUser getBasicsUser(Player player)
    {
        return this.getCore().getUserManager().getExactUser(player.getName()).attachOrGet(BasicsAttachment.class, this).getBasicsUser();
    }
}
