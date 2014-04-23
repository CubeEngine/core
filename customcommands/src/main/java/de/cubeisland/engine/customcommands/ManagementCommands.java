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
package de.cubeisland.engine.customcommands;

import java.util.Locale;

import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class ManagementCommands
{
    private final Customcommands module;
    private final CustomCommandsConfig config;

    public ManagementCommands(Customcommands module)
    {
        this.module = module;
        this.config = module.getConfig();
    }

    @Command(desc = "Adds a custom chat command.",
             indexed = {
                 @Grouped(@Indexed("name")),
                 @Grouped(value = @Indexed("message"), greedy = true)
             },
            flags = @Flag(name = "force"))
    public void add(ParameterizedContext context)
    {
        String name = context.getString(0);
        String message = context.getStrings(1);
        boolean force = context.hasFlag("force");

        if (config.commands.containsKey(name))
        {
            if (force)
            {
                config.commands.replace(name, message);
                context.sendTranslated(MessageType.POSITIVE, "Custom command !{input} has successfully been replaced.", name);
            }
            else
            {
                context.sendTranslated(MessageType.POSITIVE, "Custom command !{input} already exists. Set the flag '-force' if you want to replace the message.", name);
                return;
            }
        }
        else
        {
            config.commands.put(name.toLowerCase(Locale.ENGLISH), message);
            context.sendTranslated(MessageType.POSITIVE, "Custom command !{input} has successfully been added.", name);
        }
        config.save();
    }

    @Command(desc = "Deletes a custom chat command.",
             indexed = @Grouped(@Indexed("name")))
    public void delete(ParameterizedContext context)
    {
        String name = context.getString(0);

        if (config.commands.containsKey(name))
        {
            config.commands.remove(name.toLowerCase(Locale.ENGLISH));
            config.save();

            context.sendTranslated(MessageType.POSITIVE, "Custom command !{input} has successfully been deleted.", name);
        }
        else
        {
            context.sendTranslated(MessageType.POSITIVE, "Custom command !{input} has not been found.", name);
        }


    }
}
