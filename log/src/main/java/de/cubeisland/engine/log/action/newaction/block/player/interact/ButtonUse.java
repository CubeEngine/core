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
package de.cubeisland.engine.log.action.newaction.block.player.interact;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.BaseAction;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player using a button
 */
public class ButtonUse extends PlayerBlockAction<PlayerBlockInteractListener>
{
    // return this.lm.getConfig(world).BUTTON_USE_enable;

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof ButtonUse && this.player.equals(((PlayerBlockAction)action).player)
            && this.oldBlock == ((ButtonUse)action).oldBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count, "{user} pressed a {name#block}",
                                    "{user} pressed a {name#block} {amount} times", this.player.name,
                                    this.oldBlock.name(), count);
    }

    @Override
    public ActionCategory getCategory()
    {
        return ActionCategory.USE;
    }

    @Override
    public String getName()
    {
        return "button";
    }
}
