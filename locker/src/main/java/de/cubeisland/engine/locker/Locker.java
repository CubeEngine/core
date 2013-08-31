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

import de.cubeisland.engine.locker.BlockLockerConfiguration.BlockLockerConfigConverter;
import de.cubeisland.engine.locker.EntityLockerConfiguration.EntityLockerConfigConverter;
import de.cubeisland.engine.locker.commands.LockerCommands;
import de.cubeisland.engine.locker.commands.LockerCreateCommands;
import de.cubeisland.engine.locker.storage.LockManager;
import de.cubeisland.engine.locker.storage.TableAccessList;
import de.cubeisland.engine.locker.storage.TableLockLocations;
import de.cubeisland.engine.locker.storage.TableLocks;
import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.convert.Convert;

public class Locker extends Module
{
    private LockerConfig config;
    private LockManager manager;

    @Override
    public void onEnable()
    {
        Convert.registerConverter(BlockLockerConfiguration.class, new BlockLockerConfigConverter());
        Convert.registerConverter(EntityLockerConfiguration.class, new EntityLockerConfigConverter());
        this.config = Configuration.load(LockerConfig.class, this);
        new LockerPerm(this);
        this.getCore().getDB().registerTable(TableLocks.initTable(this.getCore().getDB()));
        this.getCore().getDB().registerTable(TableLockLocations.initTable(this.getCore().getDB()));
        this.getCore().getDB().registerTable(TableAccessList.initTable(this.getCore().getDB()));
        manager = new LockManager(this);
        LockerCommands mainCmd = new LockerCommands(this, manager);
        this.getCore().getCommandManager().registerCommand(mainCmd);
        LockerCreateCommands createCmds = new LockerCreateCommands(this, manager);
        this.getCore().getCommandManager().registerCommand(createCmds, "locker");
        new LockerListener(this, manager);
    }

    @Override
    public void onDisable()
    {
        this.manager.saveAll();
    }

    public LockerConfig getConfig()
    {
        return this.config;
    }

    /*
    Features:
    /cpersist -> so you dont have to repeat the commands (try suggesting when using a cmd alot)
    Protection:
        BlockProtection
            cprivate
            cpublic
            cremove
            cpassword
            cunlock
        ContainerProtection
            cprotect // allows looking inside the chest
      /cinfo: Owner, Type, flags, accessors, last access, creationtime
      /cmodify: Grant or take away full access to container (later support setting roles that can access)
      //"admin-access" allowing /cmodify prepend @ tp playername
      /cgive transfer ownership of a protection (you will loose acess if you are not in access-list)
      hopper protection / minecart/wHopper
      using a book as a password(key)
      locking entities: horse in particular
      lock leashknot / or fence from leashing on it
      donation chest (only allow input) + filter? /cdonation
      free chest (only allow output) + filter?
      global access list
    show notice if someone accessed your protected chest (flag)
    show if chest is protected by who (perm)
    MagnetContainer: collect Items around (config for min/max/default)
    Redstone: Block Redstone interaction (doors only?)
    AutoClose Doors/Fencegate etc.
    allow Explosion to destroy protection?
    Drop Transfer: Dropped items are teleported into selected chest
      /droptransfer select
      /droptransfer on
      /droptransfer off
      /droptransfer status
    lock beacon effects?
    purge protections:
     per user
     not accessed for a long time
     in selection
mass protect e.g. railtracks
     */
}

