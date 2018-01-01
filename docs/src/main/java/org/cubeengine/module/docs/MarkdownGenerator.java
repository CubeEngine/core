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

import static java.util.stream.Collectors.toMap;

import org.cubeengine.logscribe.Log;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.Dispatcher;
import org.cubeengine.butler.StringUtils;
import org.cubeengine.butler.alias.AliasCommand;
import org.cubeengine.butler.alias.AliasConfiguration;
import org.cubeengine.libcube.ModuleManager;
import org.cubeengine.libcube.service.command.CubeDescriptor;
import org.cubeengine.libcube.service.command.HelpCommand;
import org.cubeengine.libcube.service.permission.Permission;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.plugin.meta.PluginDependency;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

public class MarkdownGenerator implements Generator
{

    private static final String WHITESPACE = "&nbsp;";

    @Override
    public String generateList(Map<String, ModuleDocs> modules, Path modulePath, ModuleManager mm)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("# CubeEngine Documentation\n\n");
        sb.append("## Modules\n\n");

        ModuleDocs doc = modules.get("cubeengine-core");
        sb.append(" - [").append("Core").append("](modules/").append(doc.getId()).append(".md)\n");


        List<ModuleDocs> list = new ArrayList<>(modules.values());
        list.sort((a, b) -> Boolean.compare(b.isOnOre(), a.isOnOre()));
        for (ModuleDocs module : list)
        {
            if (module == doc)
            {
                continue;
            }
            sb.append(" - [").append(module.getModuleName()).append("](modules/").append(module.getId()).append(".md)");
            if (module.isWIP())
            {
                sb.append(" - [WIP]");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String generate(Log log, String id, String name, PluginContainer pc, Info info, Set<PermissionDescription> permissions,
            Set<CommandBase> commands, Permission basePermission)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(name);
        if (info.workInProgress)
        {
            sb.append(" [WIP]");
        }

        sb.append("\n");
        sb.append(pc.getDescription().orElse(""));
        sb.append("\n");
        if (info.features.isEmpty())
        {
            log.warn("Missing Features for " + name + "(" + pc.getId() + ")!");
        }
        else
        {
            sb.append("\n## Features:\n");
            for (String feature : info.features)
            {
                sb.append(" - ").append(feature).append("\n");
            }
        }

        Set<PluginDependency> plugDep = pc.getDependencies();
        if (plugDep.size() > 2) // ignore cubeengine-core and spongeapi
        {
            sb.append("\n## Dependencies:\n");
            for (PluginDependency dep : plugDep)
            {
                if (dep.getId().equals("cubeengine-core") || dep.getId().equals("spongeapi"))
                {
                    continue;
                }
                // TODO link to module or plugin on ore if possible?
                sb.append(" `").append(dep.getId()).append("`");
            }
            sb.append("\n");
        }

        if (!info.pages.isEmpty())
        {
            sb.append("\n## Pages:\n");
            for (Map.Entry<String, String> entry : info.pages.entrySet())
            {
                sb.append(" - [").append(entry.getKey()).append("]").append("(").append(id).append("-").append(entry.getValue()).append(".md)\n");
            }
        }

        TreeMap<String, PermissionDescription> addPerms = new TreeMap<>(
            permissions.stream().collect(toMap(PermissionDescription::getId, p -> p)));
        if (!commands.isEmpty())
        {
            List<CommandBase> cmds = new ArrayList<>(commands);
            cmds.sort(Comparator.comparing(o -> o.getDescriptor().getName()));
            sb.append("\n## Commands:").append("\n\n");

            sb.append("| Command | Description | Permission<br>`").append(basePermission.getId()).append(
                ".command.<perm>`").append(" |\n");
            sb.append("| --- | --- | --- |\n");
            for (CommandBase command : cmds)
            {
                generateCommandDocs(sb, addPerms, command, new Stack<>(), basePermission, true);
            }

            for (CommandBase command : cmds)
            {
                generateCommandDocs(sb, addPerms, command, new Stack<>(), basePermission, false);
            }
        }

        if (!addPerms.values().isEmpty())
        {
            sb.append("\n## Additional Permissions:\n\n");
            sb.append("| Permission | Description |\n");
            sb.append("| --- | --- |\n");
            for (PermissionDescription perm : addPerms.values())
            {
                sb.append("| `").append(perm.getId()).append("` | ").append(perm.getDescription().orElse(Text.EMPTY).toPlain()).append(" |\n");
            }
        }

        if (info.onOre != null)
        {
            sb.append("\n## [Download on Ore](https://ore.spongepowered.org/CubeEngine/").append(info.onOre).append(")\n\n");
        }

        return sb.toString();
    }

    private void generateCommandDocs(StringBuilder sb, Map<String, PermissionDescription> addPerms, CommandBase command,
                                     Stack<String> commandStack, Permission basePermission, boolean overview)
    {
        if (command instanceof AliasCommand || command instanceof HelpCommand)
        {
            return;
        }
        String id = basePermission.getId() + ".command.";

        List<CommandBase> subCommands = command instanceof Dispatcher ? new ArrayList<>(
            ((Dispatcher)command).getCommands()) : Collections.emptyList();
        subCommands.sort(Comparator.comparing(o -> o.getDescriptor().getName()));

        if (overview)
        {
            commandStack.push("*" + command.getDescriptor().getName() + "*");
            String fullCmd = StringUtils.join(WHITESPACE, commandStack);
            sb.append("| [").append(fullCmd).append("]").append("(#").append(fullCmd.replace("*", "").replace(" ",
                                                                                                              "-").replace(
                WHITESPACE, "").toLowerCase()).append(") | ");
            sb.append(command.getDescriptor().getDescription().replace("\n", "<br>")).append(" | ");
            Permission perm = ((CubeDescriptor)command.getDescriptor()).getPermission().getRegistered();
            sb.append("`").append(perm.getId().replace(id, "")).append("` |\n");

            commandStack.pop();
            commandStack.push("**" + command.getDescriptor().getName() + "**");
        }
        else
        {
            commandStack.push(command.getDescriptor().getName());
            String fullCmd = StringUtils.join(WHITESPACE, commandStack);
            sb.append("\n#### ").append(fullCmd).append("  \n");
            sb.append(command.getDescriptor().getDescription()).append("  \n");
            sb.append("**Usage:** `").append(command.getDescriptor().getUsage(null)).append("`  \n");

            if (!command.getDescriptor().getAliases().isEmpty())
            {
                sb.append("**Alias:**");
                for (AliasConfiguration alias : command.getDescriptor().getAliases())
                {
                    String[] dispatcher = alias.getDispatcher();
                    List<String> labels = new ArrayList<>();
                    if (dispatcher == null)
                    {
                        labels.add(alias.getName()); // local alias
                    }
                    else
                    {
                        labels.addAll(Arrays.asList(alias.getDispatcher()));
                        labels.add(alias.getName());
                        labels.set(0, "/" + labels.get(0));
                    }
                    sb.append(" `").append(StringUtils.join(" ", labels)).append("`");
                }
                sb.append("  \n");
            }

            if (command.getDescriptor() instanceof CubeDescriptor)
            {
                Permission perm = ((CubeDescriptor)command.getDescriptor()).getPermission().getRegistered();
                sb.append("**Permission:** `").append(perm.getId()).append("`  \n");
                addPerms.remove(perm.getId());

                // TODO parameter permission?
                // TODO parameter description?
                // TODO Butler Parser with default parameter descriptions
            }
        }

        if (!overview)
        {
            StringBuilder subBuilder = new StringBuilder();
            for (CommandBase sub : subCommands)
            {
                if (!(sub instanceof HelpCommand) && !(sub instanceof AliasCommand))
                {
                    subBuilder.append(" `").append(sub.getDescriptor().getName()).append("`");
                }
            }

            if (subBuilder.length() != 0)
            {
                sb.append("**SubCommands:**").append(subBuilder.toString());
            }
            sb.append("  \n");
        }

        for (CommandBase sub : subCommands)
        {
            this.generateCommandDocs(sb, addPerms, sub, commandStack, basePermission, overview);
        }

        commandStack.pop();
    }
}
