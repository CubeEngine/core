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
package de.cubeisland.engine.log.action.block.player.worldedit;

import org.bukkit.World;
import org.bukkit.block.BlockState;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.LogListener;

public class LogEditSession extends EditSession
{
    private final LocalPlayer player;
    private final Log module;
    private LogListener listener;

    public LogEditSession(LocalWorld world, int maxBlocks, LocalPlayer player, Log module, LogListener listener)
    {
        super(world, maxBlocks);
        this.player = player;
        this.module = module;
        this.listener = listener;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player, Log module,
                          LogListener listener)
    {
        super(world, maxBlocks, blockBag);
        this.player = player;
        this.module = module;
        this.listener = listener;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, Log module, LogListener listener)
    {
        super(world, maxBlocks);
        this.module = module;
        this.player = null;
        this.listener = listener;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, Log module, LogListener listener)
    {
        super(world, maxBlocks, blockBag);
        this.module = module;
        this.player = null;
        this.listener = listener;
    }

    @Override
    public boolean rawSetBlock(Vector pt, BaseBlock block)
    {
        if (this.player instanceof BukkitPlayer && this.player.getWorld() instanceof BukkitWorld)
        {
            World world = ((BukkitWorld)this.player.getWorld()).getWorld();
            BlockState oldState = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getState();
            boolean success = super.rawSetBlock(pt, block);
            if (success)
            {
                ActionWorldEdit action = this.listener.newAction(ActionWorldEdit.class, world);
                if (action != null)
                {
                    BlockState newState = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getState();
                    action.setOldBlock(oldState);
                    action.setNewBlock(newState);
                    action.setPlayer(((BukkitPlayer)this.player).getPlayer());
                    action.setLocation(newState.getLocation());
                    this.listener.logAction(action);
                }
            }
            return success;
        }
        return super.rawSetBlock(pt, block);
    }
}
