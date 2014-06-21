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
package de.cubeisland.engine.core.bukkit.command;

import org.bukkit.command.CommandSender;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicFactory;

public class WrappedCubeCommandHelpTopic extends GenericCommandHelpTopic
{
    private final WrappedCubeCommand command;

    public WrappedCubeCommandHelpTopic(WrappedCubeCommand command)
    {
        super(command);
        this.command = command;
    }

    @Override
    public boolean canSee(CommandSender commandSender)
    {
        String permission = command.getPermission();
        return permission == null || commandSender.hasPermission(permission);
    }

    public WrappedCubeCommand getCommand()
    {
        return command;
    }

    public static final class Factory implements HelpTopicFactory<WrappedCubeCommand>
    {
        @Override
        public HelpTopic createTopic(WrappedCubeCommand command)
        {
            return new WrappedCubeCommandHelpTopic(command);
        }
    }
}
