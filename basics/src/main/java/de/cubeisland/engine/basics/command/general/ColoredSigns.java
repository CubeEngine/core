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
package de.cubeisland.engine.basics.command.general;

import java.util.regex.Pattern;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.core.util.ChatFormat;

public class ColoredSigns implements Listener
{
    private final Basics module;

    public ColoredSigns(Basics module)
    {
        this.module = module;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        if (module.perms().SIGN_COLORED.isAuthorized(event.getPlayer())) // ALL colors
        {
            this.formatColors(event, event.getLines());
        }
        else
        {
            String toStrip = "";
            if( module.perms().SIGN_COLORED_BLACK.isAuthorized(event.getPlayer()))
            {
                toStrip += "0";
            }
            if( module.perms().SIGN_COLORED_DARK_BLUE.isAuthorized(event.getPlayer()))
            {
                toStrip += "1";
            }
            if( module.perms().SIGN_COLORED_DARK_GREEN.isAuthorized(event.getPlayer()))
            {
                toStrip += "2";
            }
            if( module.perms().SIGN_COLORED_DARK_AQUA.isAuthorized(event.getPlayer()))
            {
                toStrip += "3";
            }
            if( module.perms().SIGN_COLORED_DARK_RED.isAuthorized(event.getPlayer()))
            {
                toStrip += "4";
            }
            if( module.perms().SIGN_COLORED_DARK_PURPLE.isAuthorized(event.getPlayer()))
            {
                toStrip += "5";
            }
            if( module.perms().SIGN_COLORED_GOLD.isAuthorized(event.getPlayer()))
            {
                toStrip += "6";
            }
            if( module.perms().SIGN_COLORED_GRAY.isAuthorized(event.getPlayer()))
            {
                toStrip += "7";
            }
            if( module.perms().SIGN_COLORED_DARK_GRAY.isAuthorized(event.getPlayer()))
            {
                toStrip += "8";
            }
            if( module.perms().SIGN_COLORED_BLUE.isAuthorized(event.getPlayer()))
            {
                toStrip += "9";
            }
            if( module.perms().SIGN_COLORED_GREEN.isAuthorized(event.getPlayer()))
            {
                toStrip += "aA";
            }
            if( module.perms().SIGN_COLORED_AQUA.isAuthorized(event.getPlayer()))
            {
                toStrip += "bB";
            }
            if( module.perms().SIGN_COLORED_RED.isAuthorized(event.getPlayer()))
            {
                toStrip += "cC";
            }
            if( module.perms().SIGN_COLORED_LIGHT_PURPLE.isAuthorized(event.getPlayer()))
            {
                toStrip += "dD";
            }
            if( module.perms().SIGN_COLORED_YELLOW.isAuthorized(event.getPlayer()))
            {
                toStrip += "eE";
            }
            if( module.perms().SIGN_COLORED_WHITE.isAuthorized(event.getPlayer()))
            {
                toStrip += "fF";
            }
            if( module.perms().SIGN_COLORED_OBFUSCATED.isAuthorized(event.getPlayer()))
            {
                toStrip += "kK";
            }
            if( module.perms().SIGN_COLORED_BOLD.isAuthorized(event.getPlayer()))
            {
                toStrip += "lL";
            }
            if( module.perms().SIGN_COLORED_STRIKE.isAuthorized(event.getPlayer()))
            {
                toStrip += "mM";
            }
            if( module.perms().SIGN_COLORED_UNDERLINE.isAuthorized(event.getPlayer()))
            {
                toStrip += "nN";
            }
            if( module.perms().SIGN_COLORED_ITALIC.isAuthorized(event.getPlayer()))
            {
                toStrip += "oO";
            }
            if( module.perms().SIGN_COLORED_RESET.isAuthorized(event.getPlayer()))
            {
                toStrip += "rR";
            }
            if (toStrip.isEmpty())
            {
                return;
            }
            Pattern stripFormats = Pattern.compile("&[" + toStrip + "]");
            String[] lines = event.getLines();
            for (int i = 0; i < 4; ++i)
            {
                lines[i] = stripFormats.matcher(lines[i]).replaceAll("");
            }
            this.formatColors(event, lines);
        }
    }

    private void formatColors(SignChangeEvent event, String[] lines)
    {
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
