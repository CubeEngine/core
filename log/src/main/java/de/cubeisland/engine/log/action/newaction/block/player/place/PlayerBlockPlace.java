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
package de.cubeisland.engine.log.action.newaction.block.player.place;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockListener;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.indirect.PlayerWaterLilyBreak;

import static org.bukkit.Material.AIR;

/**
 * Represents a player placing a block
 * <p>Sub Actions:
 * <p/>
 * <p>External Actions:
 * {@link PlayerWaterLilyBreak}
 */
public class PlayerBlockPlace extends PlayerBlockActionType<PlayerBlockListener>
{
    // return "block-place";
    // return this.lm.getConfig(world).block.BLOCK_PLACE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerBlockPlace && this.player
            .equals(((PlayerBlockPlace)action).player) && ((PlayerBlockPlace)action).oldBlock == this.oldBlock && ((PlayerBlockPlace)action).newBlock == this.newBlock;
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            int amount = this.getAttached().size() + 1;
            if (this.oldBlock == AIR)
            {
                return user
                    .getTranslation(MessageType.POSITIVE, "{user} placed {amount}x {name#block}{}", this.player.name, amount, this.newBlock
                        .name());
            }
            return user
                .getTranslation(MessageType.POSITIVE, "{user} replaced {amount}x {name#block} with {name#block}{}", this.player.name, amount, this.oldBlock
                    .name(), this.newBlock.name());
        }
        // else single
        if (this.oldBlock == AIR)
        {
            return user
                .getTranslation(MessageType.POSITIVE, "{user} placed {name#block}{}", this.player.name, this.newBlock
                    .name());
        }
        return user
            .getTranslation(MessageType.POSITIVE, "{user} replaced {name#block} with {name#block}{}", this.player.name, this.oldBlock
                .name(), this.newBlock.name());
    }
}
