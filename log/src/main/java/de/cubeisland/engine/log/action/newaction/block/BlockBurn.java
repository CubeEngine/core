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
package de.cubeisland.engine.log.action.newaction.block;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.BLOCK;

/**
 * Represents a block burning away
 */
public class BlockBurn extends ActionBlock<ListenerBlock>
{
    // return this.lm.getConfig(world).block.BLOCK_BURN_enable;

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof BlockBurn && ((BlockBurn)action).oldBlock == this.oldBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count, "A {name#block} went up into flames",
                                    "{1:amount}x {name#block} went up into flames", this.oldBlock.name(), count);
    }

    @Override
    public ActionCategory getCategory()
    {
        return BLOCK;
    }

    @Override
    public String getName()
    {
        return "burn";
    }
}
