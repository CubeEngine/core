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
package de.cubeisland.engine.log.action.newaction.player.item;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.ITEM;

/**
 * Represents a player crafting an item
 */
public class CraftItem extends PlayerItemAction<PlayerItemListener>
{
    // return this.lm.getConfig(world).CRAFT_ITEM_enable;

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof CraftItem && this.player.equals(((CraftItem)action).player)
            && ((CraftItem)action).item.isSimilar(this.item);
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} crafted {name#item} x{amount}", this.player.name,
                                       this.item.getType().name(), this.getAttached().size() + 1);
        }
        return user.getTranslation(POSITIVE, "{user} crafted {name#item}", this.player.name,
                                   this.item.getType().name());
    }

    @Override
    public ActionCategory getCategory()
    {
        return ITEM;
    }

    @Override
    public String getName()
    {
        return "craft";
    }
}
