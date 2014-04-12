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
package de.cubeisland.engine.log.action.block.player.destroy;

import de.cubeisland.engine.bigdata.Reference;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.player.ActionPlayerBlock;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.BLOCK;

/**
 * Represents a player breaking a block
 * <p>SubTypes:
 * {@link PlayerNoteBlockBreak}
 * {@link PlayerSignBreak}
 * {@link PlayerJukeboxBreak}
 * {@link PlayerContainerBreak}
 */
public class PlayerBlockBreak extends ActionPlayerBlock
{
    public Reference<ActionPlayerBlock> reference;

    public PlayerBlockBreak()
    {
        super("break", BLOCK);
    }

    public PlayerBlockBreak(String name, ActionCategory... categories)
    {
        super(name, categories);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof PlayerBlockBreak && this.player.equals(((PlayerBlockBreak)action).player)
            && ((PlayerBlockBreak)action).oldBlock.material == this.oldBlock.material
            && ((this.reference == null && ((PlayerBlockBreak)action).reference == null) ||
            (this.reference != null && this.reference.equals(((PlayerBlockBreak)action).reference)));
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.reference == null)
        {
            return user.getTranslationN(POSITIVE, count,"{user} broke {name#block}",
                                        "{user} broke {name#block} x{amount}", this.player.name, this.oldBlock.name(),
                                        count);
        }
        // TODO better
        return user.getTranslationN(POSITIVE, count,
                                    "{user} broke {name#block} indirectly",
                                    "{user} broke {name#block} x{amount} indirectly",
                                    this.player.name, this.oldBlock.name(), count);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.block.destroyByPlayer;
    }
}
