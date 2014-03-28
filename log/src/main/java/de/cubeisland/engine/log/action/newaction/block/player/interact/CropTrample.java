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
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.Material.SOIL;

/**
 * Represents a player trampling crops
 */
public class CropTrample extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    //return "crop-trample";
    // return this.lm.getConfig(world).block.CROP_TRAMPLE_enable;


    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof CropTrample
            && !this.hasAttached()
            && this.player.equals(((PlayerBlockActionType)action).player)
            && 50 > Math.abs(this.date.getTime() - action.date.getTime())
            && this.coord.worldUUID.equals(action.coord.worldUUID)
            && Math.abs(this.coord.y - action.coord.y) == 1;
        // TODO xz check just to make sure?
    }

    @Override
    public String translateAction(User user)
    {
        CropTrample action = this;
        if (this.hasAttached())
        {
            if (this.oldBlock.is(SOIL))
            {
                // replacing SOIL log with the crop log as the destroyed SOIL is implied
                action = (CropTrample)this.getAttached().get(0);
            }
        }
        return user.getTranslation(POSITIVE, "{user} trampeled down {name#block}", action.player.name, action.oldBlock.name());
    }
}
