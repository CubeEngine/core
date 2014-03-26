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
package de.cubeisland.engine.log.action.newaction.block.entity;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents an Entity forming a block
 * <p>This will usually be a SnowGolem making snow
 */
public class EntityForm extends EntityBlockActionType<EntityBlockListener>
{
    // return "entity-form";
    // return this.lm.getConfig(world).block.form.ENTITY_FORM_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof EntityBreakBlock
            && ((EntityBreakBlock)action).entityType == this.entityType
            && ((EntityBreakBlock)action).newBlock == this.newBlock;
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            int count = this.countUniqueEntities();
            return user.getTranslationN(POSITIVE, count, "{text:One} {name#entity} formed {name#block} x{amount}!", "{3:amount} {name#entity} formed {name#block} x{amount}!", this.entityType
                .name(), this.oldBlock.name(), this.getAttached().size() + 1, count);
        }
        return user.getTranslation(POSITIVE, "A {name#entity} formed {name#block}",
                                   this.entityType.name(), this.oldBlock.name());
    }
}
