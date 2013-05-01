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
package de.cubeisland.cubeengine.itemrepair.repair.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.Block;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;

public class RepairBlockPersister extends SingleKeyStorage<Long,RepairBlockModel>
{
    private Map<Block,RepairBlockModel> models = new HashMap<Block, RepairBlockModel>();
    private final Module module;

    public RepairBlockPersister(Module module)
    {
        super(module.getCore().getDB(), RepairBlockModel.class, 1);
        this.module = module;
        this.initialize();
    }

    public void deleteByBlock(Block block)
    {
        RepairBlockModel repairBlockModel = this.models.remove(block);
        if (repairBlockModel != null)
        {
            this.delete(repairBlockModel);
        }
        else
        {
            this.module.getLog().warning("Could not delete model by block!");
        }
    }

    @Override
    public Collection<RepairBlockModel> getAll()
    {
        Collection<RepairBlockModel> all = super.getAll();
        for (RepairBlockModel repairBlockModel : all)
        {
            this.models.put(repairBlockModel.getBlock(this.module.getCore().getWorldManager()),repairBlockModel);
        }
        return all;
    }

    public void storeBlock(Block block, RepairBlockModel repairBlockModel)
    {
        this.store(repairBlockModel);
        this.models.put(block,repairBlockModel);
    }
}
