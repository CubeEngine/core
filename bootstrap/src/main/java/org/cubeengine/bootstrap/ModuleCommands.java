package org.cubeengine.bootstrap;
// @Command(name = "module", desc = "Provides ingame module plugin management functionality")
public class ModuleCommands
{
    // private static final String SOURCE_LINK = "https://github.com/CubeEngineDev/CubeEngine/tree/";

     /*
    TODO module commands


    @Command(desc = "Reloads the whole CubeEngine")
    public void reload(CommandSource context)
    {
        // TODO move all of reload to Plugin instead of coremodule
        i18n.sendTranslated(context, POSITIVE, "Reloading CubeEngine! This may take some time...");
        final long startTime = System.currentTimeMillis();

        PluginManager pm = Sponge.getPluginManager();

        i18n.sendTranslated(context, POSITIVE, "CubeEngine Reload completed in {integer#time}ms!",
                               System.currentTimeMillis() - startTime);
    }

    @Command(desc = "Reloads all of the modules!")
    public void reloadall(CommandSource context, @Flag boolean file)
    {
        i18n.sendTranslated(context, POSITIVE, "Reloading all modules! This may take some time...");
        Profiler.startProfiling("modulesReload");
        Modularity modulatiry = core.getModularity();
        modulatiry.getGraph().getRoot();
        long time = Profiler.endProfiling("modulesReload", TimeUnit.SECONDS);
        i18n.sendTranslated(context, POSITIVE, "Modules Reload completed in {integer#time}s!", time);
    }


    public void showSourceVersion(CommandSource context, String sourceVersion)
    {
        if (sourceVersion == null)
        {
            return;
        }
        if (sourceVersion.contains("-") && sourceVersion.length() > 40)
        {
            final String commit = sourceVersion.substring(sourceVersion.lastIndexOf('-') + 1,
                                                          sourceVersion.length() - 32);
            i18n.sendTranslated(context, POSITIVE, "Source Version: {input}", sourceVersion);
            i18n.sendTranslated(context, POSITIVE, "Source link: {input}", SOURCE_LINK + commit);
            return;
        }
        i18n.sendTranslated(context, POSITIVE, "Source Version: unknown");
    }

    @Alias(value = "modules")
    @Command(alias = "show", desc = "Lists all the loaded modules")
    public void list(CommandSource context)
    {
        Set<LifeCycle> modules = this.modularity.getModules();
        if (modules.isEmpty())
        {
            i18n.sendTranslated(context, NEUTRAL, "There are no modules loaded!");
            return;
        }
        i18n.sendTranslated(context, NEUTRAL, "These are the loaded modules.");
        i18n.sendTranslated(context, NEUTRAL, "{text:Green (+):color=BRIGHT_GREEN} stands for enabled, {text:red (-):color=RED} for disabled.");
        for (LifeCycle module : modules)
        {
            if (module.isIn(State.ENABLED))
            {
                context.sendMessage(Text.of(" + ", TextColors.GREEN, ((ModuleMetadata)module.getInformation()).getName()));
            }
            else
            {
                context.sendMessage(Text.of(" - ", TextColors.RED, ((ModuleMetadata)module.getInformation()).getName()));
            }
        }
    }

    @Command(desc = "Enables a module")
    public void enable(CommandSource context, @Reader(ModuleReader.class) Module module)
    {
        /* TODO if (this.modularity.enableModule(module))
        {
            i18n.sendTranslated(context, POSITIVE, "The given module was successfully enabled!");
            return;
        }
        i18n.sendTranslated(context, CRITICAL, "An error occurred while enabling the module!");
        *//*
}

    @Command(desc = "Disables a module")
    public void disable(CommandSource context, @Reader(ModuleReader.class) Module module)
    {
// TODO        this.modularity.disableModule(module);
        i18n.sendTranslated(context, POSITIVE, "The module {name#module} was successfully disabled!", module.getInformation().getName());
    }

    @Command(desc = "Unloaded a module and all the modules that depend on it")
    public void unload(CommandSource context, @Reader(ModuleReader.class) Module module)
    {
        // TODO  this.modularity.unloadModule(module);
        i18n.sendTranslated(context, POSITIVE, "The module {name#module} was successfully unloaded!", module.getInformation().getName());
    }

    @Command(desc = "Reloads a module")
    public void reload(CommandSource context, @Reader(ModuleReader.class) Module module, @Flag boolean file)
    {
        // TODO try
        {
            // TODO this.modularity.reloadModule(module, file);
            if (file)
            {
                i18n.sendTranslated(context, POSITIVE, "The module {name#module} was successfully reloaded from file!", module.getInformation().getName());
            }
            else
            {
                i18n.sendTranslated(context, POSITIVE, "The module {name#module} was successfully reloaded!", module.getInformation().getName());
            }
        }
        // TODO  catch (ModuleException ex)
        {
            i18n.sendTranslated(context, NEGATIVE, "Failed to reload the module!");
            i18n.sendTranslated(context, NEUTRAL, "Check the server log for info.");
            // TODO core.getLog().error(ex, "Failed to reload the module {}!", module.getInformation().getName());
        }
    }

    @Command(desc = "Loads a module from the modules directory.")
    public void load(CommandSource context, String filename)
    {
        if (filename.contains(".") || filename.contains("/") || filename.contains("\\"))
        {
            i18n.sendTranslated(context, NEGATIVE, "The given file name is invalid!");
            return;
        }
        Path modulePath = modulesFolder.resolve(filename + ".jar");
        if (!Files.exists(modulePath))
        {
            i18n.sendTranslated(context, NEGATIVE, "The given module file was not found! The name might be case sensitive.");
            return;
        }
        if (!Files.isReadable(modulePath))
        {
            i18n.sendTranslated(context, NEGATIVE, "The module exists, but cannot be read! Check the file permissions.");
            return;
        }
        // TODO check if already loaded
//        i18n.sendTranslated(context, NEUTRAL, "This module is already loaded, try reloading it.");
        fm.copyModule(modulePath);
      /*
        modularity.load(modulePath.toFile()).stream()
                  .filter(node -> node.getInformation() instanceof ModuleMetadata)
                  .forEach(node -> {
                      try
                      {
                          modularity.provide(node);
                          i18n.sendTranslated(context, POSITIVE,
                                                 "The module {name#module} has been successfully loaded and enabled!",
                                                 ((ModuleMetadata)node.getInformation()).getName());
                      }
                      catch (Exception e)
                      {
                          modularity.getProvider(Log.class).get(node.getInformation(), modularity).error(e,
                                                                                                         "Failed to load a module from file {}!",
                                                                                                         modulePath.getFileName().toString());
                          i18n.sendTranslated(context, NEGATIVE, "The module failed to load! Check the server log for info.");
                      }
                  });
                  *//*
    }

    @Command(desc = "Get info about a module")
    public void info(CommandSource context, @Reader(ModuleReader.class) Module module, @Flag boolean source)
    {
        ModuleMetadata moduleInfo = module.getInformation();
        i18n.sendTranslated(context, POSITIVE, "Name: {input}", moduleInfo.getName());
        i18n.sendTranslated(context, POSITIVE, "Description: {input}", moduleInfo.getDescription());
        i18n.sendTranslated(context, POSITIVE, "Version: {input}", moduleInfo.getVersion());
        if (source && moduleInfo.getSourceVersion() != null)
        {
            showSourceVersion(context, moduleInfo.getSourceVersion());
        }

        /* TODO
        Map<String, Version> dependencies = moduleInfo.getDependencies();
        Map<String, Version> softDependencies = moduleInfo.getSoftDependencies();
        Set<String> pluginDependencies = moduleInfo.getPluginDependencies();
        Set<String> services = moduleInfo.getServices();
        Set<String> softServices = moduleInfo.getSoftServices();
        Set<String> providedServices = moduleInfo.getProvidedServices();

        String green = "   " + ChatFormat.BRIGHT_GREEN + "- ";
        String red = "   " + ChatFormat.RED + "- ";
        if (!providedServices.isEmpty())
        {
            i18n.sendTranslated(context, POSITIVE, "Provided services:");
            for (String service : providedServices)
            {
                context.sendMessage(green + service);
            }
        }
        if (!dependencies.isEmpty())
        {
            i18n.sendTranslated(context, POSITIVE, "Module dependencies:");
            for (String dependency : dependencies.keySet())
            {
                Module dep = this.modularity.getModule(dependency);
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
            i18n.sendTranslated(context, POSITIVE, "Module soft-dependencies:");
            for (String dependency : softDependencies.keySet())
            {
                Module dep = this.modularity.getModule(dependency);
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
            i18n.sendTranslated(context, POSITIVE, "Plugin dependencies:");
            for (String dependency : pluginDependencies)
            {
                if (pm.isLoaded(dependency))
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
            i18n.sendTranslated(context, POSITIVE, "Service dependencies:");
            for (String service : services)
            {
                context.sendMessage(green + service); // TODO colors to show if service is found OR NOT
            }
        }
        if (!softServices.isEmpty())
        {
            i18n.sendTranslated(context, POSITIVE, "Service soft dependencies");
            for (String service : softServices)
            {
                context.sendMessage(green + service); // TODO colors to show if service is found OR NOT
            }
        }
          *//*

    */
}
