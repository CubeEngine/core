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
package de.cubeisland.engine.itemrepair;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.itemrepair.material.BaseMaterialContainer;
import de.cubeisland.engine.itemrepair.material.BaseMaterialContainerConverter;
import de.cubeisland.engine.itemrepair.repair.RepairBlockManager;
import de.cubeisland.engine.itemrepair.repair.storage.TableRepairBlock;

public class Itemrepair extends Module
{
    private ItemrepairConfig config;
    public RepairBlockManager repairBlockManager;

    @Override
    public void onEnable()
    {
        this.getCore().getDB().registerTable(TableRepairBlock.class);
        this.getCore().getConfigFactory().getDefaultConverterManager().
            registerConverter(BaseMaterialContainer.class, new BaseMaterialContainerConverter());
        this.config = this.loadConfig(ItemrepairConfig.class);
        this.repairBlockManager = new RepairBlockManager(this);
        this.getCore().getEventManager().registerListener(this, new ItemRepairListener(this));
        this.getCore().getCommandManager().registerCommand(new ItemRepairCommands(this));
    }

    public ItemrepairConfig getConfig()
    {
        return config;
    }

    public RepairBlockManager getRepairBlockManager()
    {
        return repairBlockManager;
    }
}
