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
package de.cubeisland.engine.core.command.commands;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.exception.ModuleAlreadyLoadedException;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.ModuleInfo;
import de.cubeisland.engine.core.module.ModuleManager;
import de.cubeisland.engine.core.module.exception.ModuleException;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.Version;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;


public class ModuleCommands extends ContainerCommand
{
    private final ModuleManager mm;

    public ModuleCommands(ModuleManager mm)
    {
        super(mm.getCoreModule(), "module", "Provides ingame module plugin management functionality");
        this.mm = mm;
    }

    @Alias(names = {
        "modules"
    })
    @Command(names = {
        "list", "show"
    }, desc = "Lists all the loaded modules", max = 0)
    public void list(CommandContext context)
    {
        Collection<Module> modules = this.mm.getModules();

        if (!modules.isEmpty())
        {
            context.sendTranslated(NEUTRAL, "These are the loaded modules.");
            context.sendTranslated(NEUTRAL, "{text:Green (+):color=BRIGHT_GREEN} stands for enabled, {text:red (-):color=RED} for disabled.");
            context.sendMessage(" ");

            for (Module module : modules)
            {
                if (module.isEnabled())
                {
                    context.sendMessage(" + " + ChatFormat.BRIGHT_GREEN + module.getName());
                }
                else
                {
                    context.sendMessage(" - " + ChatFormat.RED + module.getName());
                }
            }
        }
        else
        {
            context.sendTranslated(NEUTRAL, "There are no modules loaded!");
        }
    }

    @Command(desc = "Enables a module", usage = "<module>", min = 1, max = 1)
    public void enable(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendTranslated(NEGATIVE, "The given module could not be found!");
        }
        else if (this.mm.enableModule(module))
        {
            context.sendTranslated(POSITIVE, "The given module was successfully enabled!");
        }
        else
        {
            context.sendTranslated(CRITICAL, "An error occurred while enabling the module!");
        }
    }

    @Command(desc = "Disables a module", usage = "<module>", min = 1, max = 1)
    public void disable(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendTranslated(NEGATIVE, "The given module could not be found!");
        }
        else
        {
            this.mm.disableModule(module);
            context.sendTranslated(POSITIVE, "The module {name#module} was successfully disabled!", module.getId());
        }
    }

    @Command(desc = "Unloaded a module and all the modules that depend on it", usage = "<module>", min = 1, max = 1)
    public void unload(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendTranslated(NEGATIVE, "The given module could not be found!");
        }
        else
        {
            this.mm.unloadModule(module);
            context.sendTranslated(POSITIVE, "The module {name#module} was successfully unloaded!", module.getId());
        }
    }

    @Command(desc = "Reloads a module", usage = "<module> [-f]", min = 1, max = 1, flags = @Flag(name = "f", longName = "file"))
    public void reload(ParameterizedContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendTranslated(NEGATIVE, "The given module could not be found!");
        }
        else
        {
            try
            {
                this.mm.reloadModule(module, context.hasFlag("f"));
                if (context.hasFlag("f"))
                {
                    context.sendTranslated(POSITIVE, "The module {name#module} was successfully reloaded from file!", module.getId());
                }
                else
                {
                    context.sendTranslated(POSITIVE, "The module {name#module} was successfully reloaded!", module.getId());
                }
            }
            catch (ModuleException ex)
            {
                context.sendTranslated(NEGATIVE, "Failed to reload the module!");
                context.sendTranslated(NEUTRAL, "Check the server log for info.");
                context.getCore().getLog().error(ex, "Failed to reload the module {}!", module.getName());
            }
        }
    }

    @Command(desc = "Loads a module from the modules directory.", usage = "<file name>", min = 1, max = 1)
    public void load(CommandContext context)
    {
        String moduleFileName = context.getString(0);
        if (moduleFileName.contains(".") || moduleFileName.contains("/") || moduleFileName.contains("\\"))
        {
            context.sendTranslated(NEGATIVE, "The given file name is invalid!");
            return;
        }

        Path modulesPath = context.getCore().getFileManager().getModulesPath();

        Path modulePath = modulesPath.resolve(context.getString(0) + ".jar");
        if (!Files.exists(modulePath))
        {
            context.sendTranslated(NEGATIVE, "The given module file was not found! The name might be case sensitive.");
            return;
        }

        if (!Files.isReadable(modulePath))
        {
            context.sendTranslated(NEGATIVE, "The module exists, but cannot be read! Check the file permissions.");
            return;
        }

        try
        {
            ModuleManager mm = context.getCore().getModuleManager();
            Module module = mm.loadModule(modulePath);
            mm.enableModule(module);

            context.sendTranslated(POSITIVE, "The module {name#module} has been successfully loaded and enabled!", module.getName());
        }
        catch (ModuleAlreadyLoadedException e)
        {
            context.sendTranslated(NEUTRAL, "This module is already loaded, try reloading it.");
        }
        catch (ModuleException ex)
        {
            context.sendTranslated(NEGATIVE, "The module failed to load! Check the server log for info.");
            context.getCore().getLog().error(ex, "Failed to load a module from file {}!", modulePath);
        }
    }

    @Command(desc = "Get info about a module", flags = {
        @Flag(name = "s", longName = "source")
    }, usage = "<module> [-s]", min = 1, max = 1)
    public void info(ParameterizedContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendTranslated(NEGATIVE, "Could not find the module {name#module}!", context.getString(0));
            return;
        }
        ModuleInfo moduleInfo = module.getInfo();
        context.sendTranslated(POSITIVE, "Name: {input}", moduleInfo.getName());
        context.sendTranslated(POSITIVE, "Description: {input}", moduleInfo.getDescription());
        context.sendTranslated(POSITIVE, "Version: {input}", moduleInfo.getVersion());
        VanillaCommands.showSourceVersion(context, moduleInfo.getSourceVersion());

        Map<String, Version> dependencies = moduleInfo.getDependencies();
        Map<String, Version> softDependencies = moduleInfo.getSoftDependencies();
        Set<String> pluginDependencies = moduleInfo.getPluginDependencies();
        Set<String> services = moduleInfo.getServices();
        Set<String> providedServices = moduleInfo.getProvidedServices();

        String green = "   " + ChatFormat.BRIGHT_GREEN + "- ";
        String red = "   " + ChatFormat.RED + "- ";
        if (!providedServices.isEmpty())
        {
            context.sendTranslated(POSITIVE, "Provided services:");
            for (String service : providedServices)
            {
                context.sendMessage(green + service);
            }
        }
        if (!dependencies.isEmpty())
        {
            context.sendTranslated(POSITIVE, "Module dependencies:");
            for (String dependency : dependencies.keySet())
            {
                Module dep = this.mm.getModule(dependency);
                if (dep != null && dep.isEnabled())
                {
                    context.sendMessage(green + dependency);
                }
                else
                {
                    context.sendMessage(red + dependency);
                }
            }
        }
        if (!softDependencies.isEmpty())
        {
            context.sendTranslated(POSITIVE, "Module soft-dependencies:");
            for (String dependency : softDependencies.keySet())
            {
                Module dep = this.mm.getModule(dependency);
                if (dep != null && dep.isEnabled())
                {
                    context.sendMessage(green + dependency);
                }
                else
                {
                    context.sendMessage(red + dependency);
                }
            }
        }
        if (!pluginDependencies.isEmpty())
        {
            context.sendTranslated(POSITIVE, "Plugin dependencies:");
            for (String dependency : pluginDependencies)
            {
                Plugin dep = Bukkit.getPluginManager().getPlugin(dependency);
                if (dep != null && dep.isEnabled())
                {
                    context.sendMessage(green + dependency);
                }
                else
                {
                    context.sendMessage(red + dependency);
                }
            }
        }
        if (!services.isEmpty())
        {
            context.sendTranslated(POSITIVE, "Service dependencies:");
            for (String service : services)
            {
                context.sendMessage(green + service); // TODO colors to show if service is found OR NOT
            }
        }
        // TODO dont forget to add soft service dependencies when they're there
    }
}
