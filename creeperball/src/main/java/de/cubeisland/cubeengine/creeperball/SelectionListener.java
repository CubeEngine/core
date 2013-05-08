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
package de.cubeisland.cubeengine.creeperball;

import java.util.HashSet;

import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import de.cubeisland.cubeengine.core.user.User;

public class SelectionListener implements Listener
{
    private HashSet<User> users = new HashSet<User>();
    private final Creeperball module;

    public SelectionListener(Creeperball module)
    {
        this.module = module;
    }

    public boolean addUser(User user)
    {
        return this.users.add(user);
    }

    public boolean removeUser(User user)
    {
        return this.users.remove(user);
    }

    public void onClick(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() == null) return;
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer());
        if (!this.users.contains(user)) return;
        // TODO check for CreeperBall Tool Item
        Block block = event.getClickedBlock();
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            block = block.getRelative(event.getBlockFace());
        }
        CreeperBallAttachment attachment = user.get(CreeperBallAttachment.class);
        if (attachment == null)
        {
            throw new IllegalStateException("User is in active list but has no Attachment!");
        }
        attachment.handleBlockClick(block,event.getAction().equals(Action.LEFT_CLICK_BLOCK));
    }
}
