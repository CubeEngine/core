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
package de.cubeisland.engine.log.action.block.player.bucket;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.player.ActionPlayerBlock;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.BUCKET;
import static org.bukkit.Material.*;

/**
 * Represents a player filling a bucket
 */
public class BucketFill extends ActionPlayerBlock
{
    public BucketFill()
    {
        super("fill", BUCKET);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof BucketFill && this.player.equals(((BucketFill)action).player)
            && ((BucketFill)action).oldBlock == this.oldBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.oldBlock.is(LAVA, STATIONARY_LAVA))
        {
            return user.getTranslationN(POSITIVE, count, "{user} filled a bucket with lava",
                                        "{user} filled {amount} buckets with lava", this.player.name, count);
        }
        if (this.oldBlock.is(WATER, STATIONARY_WATER))
        {
            return user.getTranslationN(POSITIVE, count, "{user} filled a bucket with water",
                                        "{user} filled {amount} buckets with water", this.player.name, count);
        }
        return user.getTranslationN(POSITIVE, count, "{user} filled a bucket with some random fluids",
                                    "{user} filled {amount} buckets with some random fluids!", this.player.name, count);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.bucket.fill;
    }
}
