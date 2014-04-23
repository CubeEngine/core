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
package de.cubeisland.engine.log.action.block.entity;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.ENTITY;

/**
 * Represents an Entity breaking a block
 * <p>This will usually be a Zombie destroying doors
 */
public class EntityBreak extends ActionEntityBlock
{
    public EntityBreak()
    {
        super("break", ENTITY);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof EntityBreak && this.entity.isSameType(((EntityBreak)action).entity);
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            int count = this.countUniqueEntities();
            return user.getTranslationN(POSITIVE, count, "{text:One} {name#entity} destroyed {name#block} x{amount}!",
                                        "{3:amount} {name#entity} destroyed {name#block} x{amount}!",
                                        this.entity.name(), this.oldBlock.name(), this.getAttached().size() + 1, count);
        }
        return user.getTranslation(POSITIVE, "A {name#entity} destroyed {name#block}", this.entity.name(),
                                   this.oldBlock.name());
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.block.destroyByEntity;
    }
}
