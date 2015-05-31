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
package de.cubeisland.engine.module.core.util.formatter;

import de.cubeisland.engine.messagecompositor.macro.MacroContext;
import de.cubeisland.engine.messagecompositor.macro.reflected.Format;
import de.cubeisland.engine.messagecompositor.macro.reflected.Names;
import de.cubeisland.engine.messagecompositor.macro.reflected.ReflectedFormatter;
import de.cubeisland.engine.module.core.util.ChatFormat;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.util.command.CommandSource;

@Names({"user","sender","tamer"})
public class CommandSenderFormatter extends ReflectedFormatter
{
    public CommandSenderFormatter()
    {
        this.addPostProcessor(new ColorPostProcessor(ChatFormat.DARK_GREEN));
    }

    @Format
    public String format(String string, MacroContext context)
    {
        return string;
    }

    @Format
    public String format(CommandSource sender, MacroContext context)
    {
        return this.format(sender.getName(), context);
    }

    @Format
    public String format(de.cubeisland.engine.butler.CommandSource sender, MacroContext context)
    {
        return this.format(sender.getName(), context);
    }

    @Format
    public String format(Tamer tamer, MacroContext context) // includes OfflinePlayer as it implements AnimalTamer
    {
        return this.format(tamer.getName(), context);
    }

}
