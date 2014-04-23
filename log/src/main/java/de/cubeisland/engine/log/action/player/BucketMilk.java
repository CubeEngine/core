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
package de.cubeisland.engine.log.action.player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.BUCKET;

/**
 * Represents a player filling a bucket with milk
 */
public class BucketMilk extends ActionPlayer
{
    public BucketMilk()
    {
        super("milk", BUCKET);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof BucketMilk && this.player.equals(((BucketMilk)action).player);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count, "{user} milked a cow", "{user} milked {amount} cows",
                                    this.player.name, count);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.bucket.milk;
    }
}
