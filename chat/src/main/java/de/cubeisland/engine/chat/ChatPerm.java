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
        this.bindToModule(COLOR, COLOR_BLACK, COLOR_DARK_BLUE, COLOR_DARK_GREEN,
                          COLOR_DARK_AQUA, COLOR_DARK_RED, COLOR_DARK_PURPLE,
                          COLOR_GOLD, COLOR_GRAY, COLOR_DARK_GRAY,
                          COLOR_BLUE, COLOR_GREEN, COLOR_AQUA,
                          COLOR_RED, COLOR_LIGHT_PURPLE, COLOR_YELLOW,
                          COLOR_WHITE, COLOR_OBFUSCATED, COLOR_BOLD,
                          COLOR_STRIKE, COLOR_UNDERLINE, COLOR_ITALIC, COLOR_RESET);
        this.registerAllPermissions();
    }

    public static final Permission COLOR = Permission.createPermission("color");

    public static final Permission COLOR_BLACK = COLOR.createNew("black");
    public static final Permission COLOR_DARK_BLUE = COLOR.createNew("dark-blue");
    public static final Permission COLOR_DARK_GREEN = COLOR.createNew("dark-green");
    public static final Permission COLOR_DARK_AQUA = COLOR.createNew("dark-aqua");
    public static final Permission COLOR_DARK_RED = COLOR.createNew("dark-red");
    public static final Permission COLOR_DARK_PURPLE = COLOR.createNew("dark-purple");
    public static final Permission COLOR_GOLD = COLOR.createNew("gold");
    public static final Permission COLOR_GRAY = COLOR.createNew("gray");
    public static final Permission COLOR_DARK_GRAY = COLOR.createNew("dark-gray");
    public static final Permission COLOR_BLUE = COLOR.createNew("blue");
    public static final Permission COLOR_GREEN = COLOR.createNew("green");
    public static final Permission COLOR_AQUA = COLOR.createNew("aqua");
    public static final Permission COLOR_RED = COLOR.createNew("red");
    public static final Permission COLOR_LIGHT_PURPLE = COLOR.createNew("light-purple");
    public static final Permission COLOR_YELLOW = COLOR.createNew("yellow");
    public static final Permission COLOR_WHITE = COLOR.createNew("white");

    public static final Permission COLOR_OBFUSCATED = COLOR.createNew("obfuscated");
    public static final Permission COLOR_BOLD = COLOR.createNew("bold");
    public static final Permission COLOR_STRIKE = COLOR.createNew("strike");
    public static final Permission COLOR_UNDERLINE = COLOR.createNew("underline");
    public static final Permission COLOR_ITALIC = COLOR.createNew("italic");
    public static final Permission COLOR_RESET = COLOR.createNew("reset");

    static
    {
        COLOR.attach(COLOR_BLACK, COLOR_DARK_BLUE, COLOR_DARK_GREEN,
                     COLOR_DARK_AQUA, COLOR_DARK_RED, COLOR_DARK_PURPLE,
                     COLOR_GOLD, COLOR_GRAY, COLOR_DARK_GRAY,
                     COLOR_BLUE, COLOR_GREEN, COLOR_AQUA,
                     COLOR_RED, COLOR_LIGHT_PURPLE, COLOR_YELLOW,
                     COLOR_WHITE, COLOR_OBFUSCATED, COLOR_BOLD,
                     COLOR_STRIKE, COLOR_UNDERLINE, COLOR_ITALIC, COLOR_RESET);
    }
}
