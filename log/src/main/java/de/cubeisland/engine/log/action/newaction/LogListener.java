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
package de.cubeisland.engine.log.action.newaction;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.Listener;
import org.bukkit.material.Bed;

import com.mongodb.DBRefBase;
import de.cubeisland.engine.bigdata.Reference;
import de.cubeisland.engine.log.Log;

public class LogListener implements Listener
{
    protected final Log module;

    public LogListener(Log module)
    {
        this.module = module;
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

    public final <T extends ActionTypeBase<?>> T newAction(Class<T> clazz, World world)
    {
        if (!this.isActive(clazz, world))
        {
            return null;
        }
        return this.newAction(clazz);
    }

    public final <T extends ActionTypeBase<?>> T newAction(Class<T> clazz)
    {
        try
        {
            return clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new IllegalArgumentException("Given LogAction cannot be instantiated!");
        }
    }

    public final void logAction(ActionTypeBase action)
    {
        // TODO
    }

    public final boolean isActive(Class<? extends ActionTypeBase> clazz, World world)
    {
        // TODO
        return true;
    }

    public <T extends ActionTypeBase> Reference<T> reference(ActionTypeBase action)
    {
        if (action == null)
        {
            return null;
        }
        return new Reference<>(module.getCore().getConfigFactory(), new DBRefBase(db, collection, action.getId()));
    }
}
