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
package de.cubeisland.engine.log.action.block.player;

import java.util.UUID;

import org.bukkit.entity.Player;

import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.block.ActionBlock;
import de.cubeisland.engine.reflect.Section;

/**
 * Represents a player changing a block
 */
public abstract class ActionPlayerBlock extends ActionBlock
{
    public PlayerSection player;

    protected ActionPlayerBlock(String name, ActionCategory... categories)
    {
        super(name, categories);
    }

    public void setPlayer(Player player)
    {
        this.player = new PlayerSection(player);
    }

    public static class PlayerSection implements Section
    {
        public UUID uuid;
        public String name;

        public PlayerSection()
        {
        }

        public PlayerSection(Player player)
        {
            this.name = player.getName();
            this.uuid = player.getUniqueId();
        }

        public boolean equals(PlayerSection section)
        {
            return this.uuid.equals(section.uuid);
        }
    }
}
