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
package de.cubeisland.engine.log.action.block.player.destroy;

import org.bukkit.Material;

import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player breaking a jukebox
 */
public class PlayerJukeboxBreak extends PlayerBlockBreak
{
    public Material disc;

    @Override
    public String translateAction(User user)
    {
        if (disc == null || this.hasAttached())
        {
            return super.translateAction(user);
        }
        return user.getTranslation(POSITIVE, "{user} broke {name#block} with {name#item}", this.player.name,
                                   this.oldBlock.name(), this.disc.name());
    }

    public void setDisc(Material disc)
    {
        this.disc = disc;
    }
}
