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
package de.cubeisland.engine.log.action;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.Listener;
import org.bukkit.material.Bed;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import de.cubeisland.engine.bigdata.Reference;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.LoggingConfiguration;


public class LogListener implements Listener
{
    protected final Log module;
    private Class<? extends BaseAction>[] actions;

    @SafeVarargs
    public LogListener(Log module, Class<? extends BaseAction>... actions)
    {
        this.module = module;
        this.actions = actions;
    }

    public Class<? extends BaseAction>[] getActions()
    {
        return actions;
    }

    /**
     * Only the bottom half of doors and the feet of a bed is logged!
     *
     * @param blockState the blockstate to adjust
     *
     * @return the adjusted blockstate
     */
    public static BlockState adjustBlockForDoubleBlocks(BlockState blockState)
    {
        if (blockState.getType() == Material.WOODEN_DOOR || blockState.getType() == Material.IRON_DOOR_BLOCK)
        {
            if (blockState.getRawData() == 8 || blockState.getRawData() == 9)
            {
                if (blockState.getRawData() == 9)
                {
                    blockState = blockState.getBlock().getRelative(BlockFace.DOWN).getState();
                    blockState.setRawData((byte)(blockState.getRawData() + 8));
                    return blockState;
                }
                return blockState.getBlock().getRelative(BlockFace.DOWN).getState();
            }
            else
            {
                if (blockState.getBlock().getRelative(BlockFace.UP).getState().getRawData() == 9)
                {
                    blockState.setRawData((byte)(blockState.getRawData() + 8));
                }
            }
        }
        else if (blockState.getData() instanceof Bed)
        {
            Bed bed = (Bed)blockState.getData();
            if (bed.isHeadOfBed())
            {
                return blockState.getBlock().getRelative(bed.getFacing().getOppositeFace()).getState();
            }
        }
        return blockState;
    }

    public final <T extends BaseAction> T newAction(Class<T> clazz, World world)
    {
        if (!this.isActive(clazz, world))
        {
            return null;
        }
        return this.newAction(clazz);
    }

    public final <T extends BaseAction> T newAction(Class<T> clazz)
    {
        T action = module.getCore().getConfigFactory().create(clazz);
        action.setTarget(new BasicDBObject());
        return action;
    }

    public final void logAction(BaseAction action)
    {
        this.module.getLogManager().queueLog(action);
    }

    public final boolean isActive(Class<? extends BaseAction> clazz, World world)
    {
        LoggingConfiguration config = this.getConfig(world);
        this.module.getActionManager().isActive(clazz, config);
        return true;
    }

    public <T extends BaseAction> Reference<T> reference(BaseAction action)
    {
        if (action == null)
        {
            return null;
        }
        DBCollection collection = module.getLogManager().getCollection();
        return new Reference<>(module.getCore().getConfigFactory(), collection, action.getTarget());
    }

    public LoggingConfiguration getConfig(World world)
    {
        return this.module.getLogManager().getConfig(world);
    }
}
