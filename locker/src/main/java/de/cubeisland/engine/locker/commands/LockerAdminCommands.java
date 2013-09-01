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
package de.cubeisland.engine.locker.commands;

import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.locker.Locker;
import de.cubeisland.engine.locker.storage.LockManager;

public class LockerAdminCommands extends ContainerCommand
{
    private LockManager manager;

    public LockerAdminCommands(Locker module, LockManager manager)
    {
        super(module, "admin", "Administrate the protections");
        this.manager = manager;
    }
    // view (open chest)
    // find (show all protections)
    // forceowner change owner
    // remove by id
    // purge remove of player / in selection
    // cleanup remove not accessed protections / time in config
    // tp tp to the location of the protection
}