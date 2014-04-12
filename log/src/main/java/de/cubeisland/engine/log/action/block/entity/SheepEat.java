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
 * Represents a Sheep eating grass
 */
public class SheepEat extends ActionEntityBlock
{
    public SheepEat()
    {
        super("sheep-eat", ENTITY);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof SheepEat;
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            int count = this.countUniqueEntities();
            return user.getTranslationN(POSITIVE, count, "{text:One sheep} ate {text:grass} x{amount}!",
                                        "{1:amount} {text:sheep} ate {text:grass} x{amount}!",
                                        this.getAttached().size() + 1, count);
        }
        return user.getTranslation(POSITIVE, "A {text#sheep} ate {text:grass}");
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.block.sheepEat;
    }
}
