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
package de.cubeisland.engine.log.action.newaction.block.player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.BaseAction;
import de.cubeisland.engine.log.action.newaction.block.player.bucket.PlayerLavaBucketPlace;
import de.cubeisland.engine.log.action.newaction.block.player.bucket.PlayerWaterBucketPlace;

import static de.cubeisland.engine.log.action.ActionCategory.BLOCK;
import static org.bukkit.Material.AIR;

/**
 * Represents a player placing a block
 * <p>SubActions:
 * {@link PlayerLavaBucketPlace}
 * {@link PlayerWaterBucketPlace}
 */
public class PlayerBlockPlace extends PlayerBlockAction<PlayerBlockListener>
{
    // return this.lm.getConfig(world).block.BLOCK_PLACE_enable;

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
                return user.getTranslation(MessageType.POSITIVE, "{user} placed {amount}x {name#block}",
                                           this.player.name, amount, this.newBlock.name());
            }
            return user.getTranslation(MessageType.POSITIVE,
                                       "{user} replaced {amount}x {name#block} with {name#block}", this.player.name,
                                       amount, this.oldBlock.name(), this.newBlock.name());
        }
        // else single
        if (this.oldBlock.is(AIR))
        {
            return user.getTranslation(MessageType.POSITIVE, "{user} placed {name#block}", this.player.name,
                                       this.newBlock.name());
        }
        return user.getTranslation(MessageType.POSITIVE, "{user} replaced {name#block} with {name#block}",
                                   this.player.name, this.oldBlock.name(), this.newBlock.name());
    }

    @Override
    public ActionCategory getCategory()
    {
        return BLOCK;
    }

    @Override
    public String getName()
    {
        return "place";
    }
}
