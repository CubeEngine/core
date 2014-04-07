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

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.ListenerBlock;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.BLOCK;
import static org.bukkit.Material.AIR;

/**
 * Represents a player letting a tree or mushroom grow using bonemeal
 */
public class PlayerBlockGrow extends ActionPlayerBlock
{
    public PlayerBlockGrow()
    {
        super("grow", BLOCK);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof PlayerBlockGrow && this.player.equals(((PlayerBlockGrow)action).player)
            && ((PlayerBlockGrow)action).oldBlock == this.oldBlock
            && ((PlayerBlockGrow)action).newBlock == this.newBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.oldBlock.is(AIR))
        {
            return user.getTranslationN(POSITIVE, count, "{user} let grow {name#block}",
                                        "{user} let grow {2:amount}x {name#block}", this.player.name,
                                        this.newBlock.name(), count);
        }
        return user.getTranslationN(POSITIVE, count, "{user} let grow {name#block} into {name#block}",
                                    "{user} let grow {3:amount}x {name#block} into {name#block}", this.player.name,
                                    this.newBlock.name(), this.oldBlock.name(), count);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.block.growByPlayer;
    }
}
