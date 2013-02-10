package de.cubeisland.cubeengine.core.command.commands;

import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.util.ChatFormat;

import java.util.Collection;

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
            context.sendMessage("core", "These are the loaded modules.");
            context.sendMessage("core", "&aGreen (+)&r stands for enabled, &cred (-)&r for disabled.");
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
            context.sendMessage("core", "&eThere are no modules loaded!");
        }
    }

    @Command(desc = "Enables a module", usage = "<module>", min = 1, max = 1)
    public void enable(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendMessage("core", "The given module could not be found!");
        }
        else if (this.mm.enableModule(module))
        {
            context.sendMessage("core", "The given module was successfully enabled!");
        }
        else
        {
            context.sendMessage("core", "An error occurred while enabling the module!");
        }
    }

    @Command(desc = "Disables a module", usage = "<module>", min = 1, max = 1)
    public void disable(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendMessage("core", "The given module could not be found!");
        }
        else
        {
            this.mm.disableModule(module);
            context.sendMessage("core", "The given module was successfully disabled!");
        }
    }

    @Command(desc = "Unloaded a module and all the modules that depend on it", usage = "<module>", min = 1, max = 1)
    public void unload(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendMessage("core", "The given module could not be found!");
        }
        else
        {
            this.mm.unloadModule(module);
            context.sendMessage("core", "The given module was successfully unloaded!");
        }
    }

    @Command(desc = "Reloads a module", usage = "<module>", min = 1, max = 1)
    public void reload(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendMessage("core", "The given module could not be found!");
        }
        else
        {
            this.mm.reloadModule(module);
        }
    }
}
