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
package de.cubeisland.engine.log.action.logaction.worldedit;

import org.bukkit.World;
import org.bukkit.block.BlockState;

import de.cubeisland.engine.log.Log;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;

public class LogEditSession extends EditSession
{
    private final LocalPlayer player;
    private final Log module;

    public LogEditSession(LocalWorld world, int maxBlocks, LocalPlayer player, Log module)
    {
        super(world, maxBlocks);
        this.player = player;
        this.module = module;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player, Log module)
    {
        super(world, maxBlocks, blockBag);
        this.player = player;
        this.module = module;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, Log module)
    {
        super(world, maxBlocks);
        this.module = module;
        this.player = null;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, Log module)
    {
        super(world, maxBlocks, blockBag);
        this.module = module;
        this.player = null;
    }

    @Override
    public boolean rawSetBlock(Vector pt, BaseBlock block)
    {
        if (this.player instanceof BukkitPlayer && this.player.getWorld() instanceof BukkitWorld )
        {
            World world = ((BukkitWorld)this.player.getWorld()).getWorld();
            BlockState oldState = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getState();
            boolean success = super.rawSetBlock(pt, block);
            if (success)
            {
                WorldEditActionType actionType = this.module.getActionTypeManager().getActionType(WorldEditActionType.class);
                if (actionType.isActive(world))
                {
                    BlockState newState =  world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getState();
                    actionType.logBlockChange(((BukkitPlayer)this.player).getPlayer(),oldState,newState,null);
                }
            }
            return success;
        }
        return super.rawSetBlock(pt, block);
    }
}
