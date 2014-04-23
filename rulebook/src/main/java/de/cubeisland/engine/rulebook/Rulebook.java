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
package de.cubeisland.engine.rulebook;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.rulebook.bookManagement.RulebookCommands;
import de.cubeisland.engine.rulebook.bookManagement.RulebookManager;

public class Rulebook extends Module
{
    private RulebookManager rulebookManager;

    @Override
    public void onEnable()
    {
        // this.getCore().getFileManager().dropResources(RulebookResource.values());
        Permission perm = this.getBasePermission().
            childWildcard("command").
                                  childWildcard("get").
            child("other");
        this.getCore().getPermissionManager().registerPermission(this, perm);

        this.rulebookManager = new RulebookManager(this);

        this.getCore().getCommandManager().registerCommand(new RulebookCommands(this));
        this.getCore().getEventManager().registerListener(this, new RulebookListener(this));
    }

    public RulebookManager getRuleBookManager()
    {
        return this.rulebookManager;
    }
}
