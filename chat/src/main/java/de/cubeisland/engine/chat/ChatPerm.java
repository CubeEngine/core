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
package de.cubeisland.engine.chat;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;

public class ChatPerm extends PermissionContainer<Chat>
{
    public ChatPerm(Chat module)
    {
        super(module);
        COLOR.attach(COLOR_BLACK, COLOR_DARK_BLUE, COLOR_DARK_GREEN,
                     COLOR_DARK_AQUA, COLOR_DARK_RED, COLOR_DARK_PURPLE,
                     COLOR_GOLD, COLOR_GRAY, COLOR_DARK_GRAY,
                     COLOR_BLUE, COLOR_GREEN, COLOR_AQUA,
                     COLOR_RED, COLOR_LIGHT_PURPLE, COLOR_YELLOW,
                     COLOR_WHITE, COLOR_OBFUSCATED, COLOR_BOLD,
                     COLOR_STRIKE, COLOR_UNDERLINE, COLOR_ITALIC, COLOR_RESET);
        this.registerAllPermissions();
    }

    public final Permission COLOR = getBasePerm().child("color");

    public final Permission COLOR_BLACK = COLOR.newPerm("black");
    public final Permission COLOR_DARK_BLUE = COLOR.newPerm("dark-blue");
    public final Permission COLOR_DARK_GREEN = COLOR.newPerm("dark-green");
    public final Permission COLOR_DARK_AQUA = COLOR.newPerm("dark-aqua");
    public final Permission COLOR_DARK_RED = COLOR.newPerm("dark-red");
    public final Permission COLOR_DARK_PURPLE = COLOR.newPerm("dark-purple");
    public final Permission COLOR_GOLD = COLOR.newPerm("gold");
    public final Permission COLOR_GRAY = COLOR.newPerm("gray");
    public final Permission COLOR_DARK_GRAY = COLOR.newPerm("dark-gray");
    public final Permission COLOR_BLUE = COLOR.newPerm("blue");
    public final Permission COLOR_GREEN = COLOR.newPerm("green");
    public final Permission COLOR_AQUA = COLOR.newPerm("aqua");
    public final Permission COLOR_RED = COLOR.newPerm("red");
    public final Permission COLOR_LIGHT_PURPLE = COLOR.newPerm("light-purple");
    public final Permission COLOR_YELLOW = COLOR.newPerm("yellow");
    public final Permission COLOR_WHITE = COLOR.newPerm("white");

    public final Permission COLOR_OBFUSCATED = COLOR.newPerm("obfuscated");
    public final Permission COLOR_BOLD = COLOR.newPerm("bold");
    public final Permission COLOR_STRIKE = COLOR.newPerm("strike");
    public final Permission COLOR_UNDERLINE = COLOR.newPerm("underline");
    public final Permission COLOR_ITALIC = COLOR.newPerm("italic");
    public final Permission COLOR_RESET = COLOR.newPerm("reset");

    public final Permission COMMAND_NICK_OTHER = getBasePerm().childWildcard("command").child("nick").child("other");
    /**
     * Allows to set the nickname to a players name that plays on this server
     */
    public final Permission COMMAND_NICK_OFOTHER = getBasePerm().childWildcard("command").child("nick").child("of-other");
}
