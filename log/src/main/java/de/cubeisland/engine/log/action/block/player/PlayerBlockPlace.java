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

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.BLOCK;
import static org.bukkit.Material.AIR;

/**
 * Represents a player placing a block
 * <p>SubActions:
 * {@link de.cubeisland.engine.log.action.block.player.bucket.BucketLava}
 * {@link de.cubeisland.engine.log.action.block.player.bucket.BucketWater}
 */
public class PlayerBlockPlace extends ActionPlayerBlock
{
    public PlayerBlockPlace()
    {
        super("place", BLOCK);
    }

    public PlayerBlockPlace(String name, ActionCategory... categories)
    {
        super(name, categories);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof PlayerBlockPlace && this.player.equals(((PlayerBlockPlace)action).player)
            && ((PlayerBlockPlace)action).oldBlock.material == this.oldBlock.material
            && ((PlayerBlockPlace)action).newBlock.material == this.newBlock.material;
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            int amount = this.getAttached().size() + 1;
            if (this.oldBlock.is(AIR))
            {
                return user.getTranslation(POSITIVE, "{user} placed {amount}x {name#block}",
                                           this.player.name, amount, this.newBlock.name());
            }
            return user.getTranslation(POSITIVE, "{user} replaced {amount}x {name#block} with {name#block}",
                                       this.player.name, amount, this.oldBlock.name(), this.newBlock.name());
        }
        // else single
        if (this.oldBlock.is(AIR))
        {
            return user.getTranslation(POSITIVE, "{user} placed {name#block}", this.player.name,
                                       this.newBlock.name());
        }
        return user.getTranslation(POSITIVE, "{user} replaced {name#block} with {name#block}",
                                   this.player.name, this.oldBlock.name(), this.newBlock.name());
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.block.placeByPlayer;
    }
}
