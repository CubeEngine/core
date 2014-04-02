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
package de.cubeisland.engine.log.action.newaction.block.player.destroy;

import de.cubeisland.engine.bigdata.Reference;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.BaseAction;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockAction;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockListener;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.BLOCK;

/**
 * Represents a player breaking a block
 * <p>SubTypes:
 * {@link PlayerNoteBlockBreak}
 * {@link PlayerSignBreak}
 * {@link PlayerJukeboxBreak}
 * {@link PlayerContainerBreak}
 */
public class PlayerBlockBreak extends PlayerBlockAction<PlayerBlockListener>
{
    // return this.lm.getConfig(world).block.BLOCK_BREAK_enable;

    public Reference<PlayerBlockAction> reference; // TODO use in message

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof PlayerBlockBreak && this.player.equals(((PlayerBlockBreak)action).player)
            && ((PlayerBlockBreak)action).oldBlock.material == this.oldBlock.material;
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} broke {name#block} x{amount}", this.player.name,
                                       this.oldBlock.name(), this.countAttached());
        }
        return user.getTranslation(POSITIVE, "{user} broke {name#block}", this.player.name, this.oldBlock.name());
    }

    @Override
    public ActionCategory getCategory()
    {
        return BLOCK;
    }

    @Override
    public String getName()
    {
        return "break";
    }
}
