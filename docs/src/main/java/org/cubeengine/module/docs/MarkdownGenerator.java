/*
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
package org.cubeengine.module.docs;

import de.cubeisland.engine.logscribe.Log;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.Dispatcher;
import org.cubeengine.butler.StringUtils;
import org.cubeengine.butler.alias.AliasCommand;
import org.cubeengine.butler.alias.AliasConfiguration;
import org.cubeengine.libcube.service.command.CubeDescriptor;
import org.cubeengine.libcube.service.command.HelpCommand;
import org.cubeengine.libcube.service.permission.Permission;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class MarkdownGenerator implements Generator {

    public String generate(Log log, String name, PluginContainer pc, Info info, Set<Permission> permissions, Set<CommandBase> commands)
    {

        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(name);
        if (info.workInProgress)
        {
            sb.append(" [WIP]");
        }
        sb.append("\n\n");
        sb.append(pc.getDescription().orElse(""));
        sb.append("\n\n");
        if (info.features.isEmpty())
        {
            log.warn("Missing Features for " + name + "!");
        }
        else
        {
            sb.append("## Features:\n");
            for (String feature : info.features)
            {
                sb.append(" - ").append(feature).append("\n");
            }
        }

        TreeMap<String, Permission> addPerms = new TreeMap<>(permissions.stream().collect(toMap(Permission::getId, p -> p)));
        if (!commands.isEmpty())
        {
            sb.append("\n\n").append("## Commands").append("\n");
            for (CommandBase command : commands)
            {
                generateCommandDocs(sb, addPerms, command, new Stack<>());
            }

        }

        if (!addPerms.values().isEmpty())
        {
            sb.append("\n## Additional Permissions\n");

            for (Permission perm : addPerms.values())
            {
                sb.append(" - ##### `").append(perm.getId()).append("`\n");
                sb.append("   ").append(perm.getDesc()).append("\n\n");
            }
        }

        return sb.toString();
    }

    private void generateCommandDocs(StringBuilder sb, Map<String, Permission> addPerms, CommandBase command, Stack<String> commandStack)
    {
        if (command instanceof AliasCommand || command instanceof HelpCommand)
        {
            return;
        }
        commandStack.push(command.getDescriptor().getName());
        sb.append("\n#### ").append(StringUtils.join(" ", commandStack)).append("\n");
        sb.append(command.getDescriptor().getDescription()).append("\n\n");
        sb.append("Usage: `").append(command.getDescriptor().getUsage(null)).append("`");

        if (!command.getDescriptor().getAliases().isEmpty())
        {
            sb.append("\n\nAlias:");
            for (AliasConfiguration alias : command.getDescriptor().getAliases())
            {
                String[] dispatcher = alias.getDispatcher();
                List<String> labels = new ArrayList<>();
                if (dispatcher == null) {
                    labels.add(alias.getName()); // local alias
                } else {
                    labels.addAll(Arrays.asList(alias.getDispatcher()));
                    labels.add(alias.getName());
                    labels.set(0, "/" + labels.get(0));
                }
                sb.append("\n`").append(StringUtils.join(" ", labels)).append("`");
            }
        }
        sb.append("\n\n");

        if (command.getDescriptor() instanceof CubeDescriptor)
        {
            Permission perm = ((CubeDescriptor) command.getDescriptor()).getPermission().getRegistered();
            sb.append("Permission: `").append(perm.getId()).append("`\n");
            addPerms.remove(perm.getId());

            // TODO parameter permission?
            // TODO parameter description?
            // TODO Butler Parser with default parameter descriptions
        }

        if (command instanceof Dispatcher)
        {
            StringBuilder subBuilder = new StringBuilder();
            List<CommandBase> subCommands = new ArrayList<>(((Dispatcher) command).getCommands());
            subCommands.sort(Comparator.comparing(o -> o.getDescriptor().getName()));
            for (CommandBase sub : subCommands)
            {
                if (!(sub instanceof HelpCommand) && !(sub instanceof AliasCommand))
                {
                    subBuilder.append("\n`").append(sub.getDescriptor().getName()).append("`");
                }
            }

            if (subBuilder.length() != 0)
            {
                sb.append("\nChild Commands:").append(subBuilder.toString());
            }

            for (CommandBase sub : subCommands)
            {
                this.generateCommandDocs(sb, addPerms, sub, commandStack);
            }
        }
        commandStack.pop();
    }
}
