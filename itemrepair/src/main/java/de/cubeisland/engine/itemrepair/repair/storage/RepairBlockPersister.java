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
package de.cubeisland.engine.itemrepair.repair.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Block;

import de.cubeisland.engine.core.module.Module;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.itemrepair.repair.storage.TableRepairBlock.TABLE_REPAIR_BLOCK;

public class RepairBlockPersister
{
    private final Map<Block,RepairBlockModel> models = new HashMap<>();
    private final Module module;
    private final DSLContext dsl;

    public RepairBlockPersister(Module module)
    {
        this.dsl = module.getCore().getDB().getDSL();
        this.module = module;
    }

    public void deleteByBlock(Block block)
    {
        RepairBlockModel repairBlockModel = this.models.remove(block);
        if (repairBlockModel != null)
        {
            repairBlockModel.delete();
        }
        else
        {
            this.module.getLog().warn("Could not delete model by block!");
        }
    }

    public Collection<RepairBlockModel> getAll(World world)
    {
        Collection <RepairBlockModel> all = this.dsl.selectFrom(TABLE_REPAIR_BLOCK)
            .where(TABLE_REPAIR_BLOCK.WORLD.eq(UInteger.valueOf(this.module.getCore().getWorldManager().getWorldId(world)))).fetch();
        for (RepairBlockModel repairBlockModel : all)
        {
            this.models.put(repairBlockModel.getBlock(this.module.getCore().getWorldManager()),repairBlockModel);
        }
        return all;
    }

    public void storeBlock(Block block, RepairBlockModel repairBlockModel)
    {
        repairBlockModel.insert();
        this.models.put(block,repairBlockModel);
    }
}
