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
package de.cubeisland.engine.log.action.newaction.block.player.worldedit;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.BaseAction;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.BLOCK;
import static org.bukkit.Material.AIR;

/**
 * Represents a player changing blocks using the worldedit plugin
 */
public class WorldEditAction extends PlayerBlockAction<LogEditSession>
{
    // return this.lm.getConfig(world).block.WORLDEDIT_enable;

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof WorldEditAction && this.player.equals(((WorldEditAction)action).player)
            && this.newBlock.material == ((WorldEditAction)action).newBlock.material
            && this.oldBlock.material == ((WorldEditAction)action).oldBlock.material;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.newBlock.is(AIR))
        {
            return user.getTranslationN(POSITIVE, count, "{user} used worldedit to remove {name#block}",
                                        "{user} used worldedit to remove {name#block} x{amount}", this.player.name,
                                        this.oldBlock.name(), count);
        }
        else if (this.oldBlock.is(AIR))
        {
            return user.getTranslationN(POSITIVE, count, "{user} used worldedit to place {name#block}",
                                        "{user} used worldedit to place {name#block} x{amount}", this.player.name,
                                        this.newBlock.name(), count);
        }
        else
        {
            return user.getTranslationN(POSITIVE, count,
                                        "{user} used worldedit to replace {name#block} with {name#block}",
                                        "{user} used worldedit to replace {name#block} with {name#block} x{amount}",
                                        this.player.name, this.oldBlock.name(), this.newBlock.name(), count);
        }
    }

    @Override
    public ActionCategory getCategory()
    {
        return BLOCK;
    }

    @Override
    public String getName()
    {
        return "worldedit";
    }
}
