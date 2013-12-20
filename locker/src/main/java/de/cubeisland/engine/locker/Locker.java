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
package de.cubeisland.engine.locker;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.Reloadable;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.locker.BlockLockerConfiguration.BlockLockerConfigConverter;
import de.cubeisland.engine.locker.EntityLockerConfiguration.EntityLockerConfigConverter;
import de.cubeisland.engine.locker.commands.LockerAdminCommands;
import de.cubeisland.engine.locker.commands.LockerCommands;
import de.cubeisland.engine.locker.commands.LockerCreateCommands;
import de.cubeisland.engine.locker.storage.LockManager;
import de.cubeisland.engine.locker.storage.TableAccessList;
import de.cubeisland.engine.locker.storage.TableLockLocations;
import de.cubeisland.engine.locker.storage.TableLocks;

public class Locker extends Module implements Reloadable
{
    private LockerConfig config;
    private LockManager manager;
    private LockerListener listener;

    @Override
    public void onEnable()
    {
        ConverterManager cManager = this.getCore().getConfigFactory().getDefaultConverterManager();
        cManager.registerConverter(BlockLockerConfiguration.class, new BlockLockerConfigConverter());
        cManager.registerConverter(EntityLockerConfiguration.class, new EntityLockerConfigConverter());
        this.config = this.loadConfig(LockerConfig.class);
        Database db = this.getCore().getDB();
        db.registerTable(TableLocks.class);
        db.registerTable(TableLockLocations.class);
        db.registerTable(TableAccessList.class);
        manager = new LockManager(this);
        LockerCommands mainCmd = new LockerCommands(this, manager);
        this.getCore().getCommandManager().registerCommand(mainCmd);
        this.getCore().getCommandManager().registerCommand(new LockerCreateCommands(this, manager), "locker");
        this.getCore().getCommandManager().registerCommand(new LockerAdminCommands(this, manager), "locker");
        new LockerPerm(this, mainCmd);
        listener = new LockerListener(this, manager);
    }

    @Override
    public void onDisable()
    {
        this.manager.saveAll();
        this.getCore().getEventManager().removeListeners(this);
        this.getCore().getCommandManager().removeCommands(this);
    }

    @Override
    public void reload()
    {
        this.onDisable();
        this.config = this.loadConfig(LockerConfig.class);
        Database db = this.getCore().getDB();
        db.registerTable(TableLocks.class);
        db.registerTable(TableLockLocations.class);
        db.registerTable(TableAccessList.class);
        manager = new LockManager(this);
        LockerCommands mainCmd = new LockerCommands(this, manager);
        this.getCore().getCommandManager().registerCommand(mainCmd);
        this.getCore().getCommandManager().registerCommand(new LockerCreateCommands(this, manager), "locker");
        this.getCore().getCommandManager().registerCommand(new LockerAdminCommands(this, manager), "locker");
        listener = new LockerListener(this, manager);
    }

    public LockerConfig getConfig()
    {
        return this.config;
    }

    /*
    Features:
      lock leashknot / or fence from leashing on it
    lock beacon effects?
    golden masterKey for Admin/Mod to open all chests (explode in hand if no perm)
    masterKeys to open all chests of one user
    multiKeys to open multiple chests
    buttons to door-protection to open door for x-sec = autoclose time BUT deny redstone so ONLY that button can open the door/doubledoor
    implement usage of separate access-level for in & out in containers
    name a protection
     */

}

