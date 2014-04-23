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

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.log.action.ActionCategory;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.SIGN;

/**
 * Represents a player breaking a sign
 */
public class PlayerSignBreak extends PlayerBlockBreak
{
    public String[] oldLines;

    public PlayerSignBreak()
    {
        super("break", SIGN);
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return super.translateAction(user);
        }
        String delim = ChatFormat.GREY + " | " + ChatFormat.GOLD;
        return super.translateAction(user) + "\n" +
            user.getTranslation(POSITIVE, "    with {input#signtext} written on it", StringUtils.implode(delim,
                                                                                                         this.oldLines));
    }

    public void setLines(String[] lines)
    {
        this.oldLines = lines;
    }

    // TODO custom rollback/redo
}
