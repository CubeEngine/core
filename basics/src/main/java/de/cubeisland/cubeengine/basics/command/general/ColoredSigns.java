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
package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class ColoredSigns implements Listener
{
    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        if (BasicsPerm.SIGN_COLORED.isAuthorized(event.getPlayer()))
        {
            String[] lines = event.getLines();
            for (int i = 0; i < 4; ++i)
            {
                lines[i] = ChatFormat.parseFormats(lines[i]);
            }
            for (int i = 0; i < 4; ++i)
            {
                event.setLine(i, lines[i]);
            }
        }
    }
}
