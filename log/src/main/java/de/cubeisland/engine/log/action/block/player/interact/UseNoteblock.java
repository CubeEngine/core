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
package de.cubeisland.engine.log.action.block.player.interact;

import java.util.concurrent.TimeUnit;

import org.bukkit.block.BlockState;
import org.bukkit.block.NoteBlock;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.player.ActionPlayerBlock;

import static de.cubeisland.engine.log.action.ActionCategory.USE;

/**
 * Represents a player changing the tune of a noteblock
 */
public class UseNoteblock extends ActionPlayerBlock
{
    public byte note;

    public UseNoteblock()
    {
        super("noteblock", USE);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof UseNoteblock && this.player.equals(((ActionPlayerBlock)action).player)
            && this.isNearTimeFrame(TimeUnit.MINUTES, 2, action);
    }

    @Override
    public String translateAction(User user)
    {
        byte newNote = (byte)(this.note + 1);
        if (this.hasAttached())
        {
            newNote = (byte)(((UseNoteblock)this.getAttached().get(this.getAttached().size() - 1)).note + 1);
        }
        newNote %= 25;
        if (this.note == newNote)
        {
            return user.getTranslation(MessageType.POSITIVE,
                                       "{user} fiddled around with the noteblock but did not change anything",
                                       this.player.name);
        }
        return user.getTranslation(MessageType.POSITIVE, "{user} set the noteblock to {amount} clicks",
                                   this.player.name, newNote);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setOldBlock(BlockState state)
    {
        super.setOldBlock(state);
        this.note = ((NoteBlock)state).getNote().getId();
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.use.noteblock;
    }
}
