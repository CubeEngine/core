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
package de.cubeisland.cubeengine.itemrepair;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.itemrepair.repair.RepairBlockManager;

public class Itemrepair extends Module
{
    private ItemrepairConfig config;
    private Conomy conomy;
    public RepairBlockManager repairBlockManager;

    @Override
    public void onEnable()
    {
        this.repairBlockManager = new RepairBlockManager(this);
        this.getCore().getEventManager().registerListener(this, new ItemRepairListener(this));
        this.getCore().getCommandManager().registerCommand(new ItemRepairCommands(this));
    }

    public ItemrepairConfig getConfig()
    {
        return config;
    }

    public Conomy getConomy()
    {
        return conomy;
    }

    public RepairBlockManager getRepairBlockManager()
    {
        return repairBlockManager;
    }
}
