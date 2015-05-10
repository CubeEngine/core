package de.cubeisland.engine.module.core;

import java.util.concurrent.TimeUnit;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import de.cubeisland.engine.butler.parametric.Command;
import de.cubeisland.engine.butler.parametric.Desc;
import de.cubeisland.engine.butler.parametric.Flag;
import de.cubeisland.engine.butler.parametric.Optional;
import de.cubeisland.engine.butler.parametric.Reader;
import de.cubeisland.engine.logscribe.LogLevel;
import de.cubeisland.engine.module.core.command.CommandContext;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.command.ContainerCommand;
import de.cubeisland.engine.module.core.sponge.SpongeCore;
import de.cubeisland.engine.module.core.user.User;
import de.cubeisland.engine.module.core.user.UserManager;
import de.cubeisland.engine.module.core.util.Profiler;
import org.spongepowered.api.plugin.PluginManager;

import static de.cubeisland.engine.module.core.util.formatter.MessageType.NEUTRAL;
import static de.cubeisland.engine.module.core.util.formatter.MessageType.POSITIVE;

@Command(name = "cubeengine", alias = "ce",
    desc = "These are the basic commands of the CubeEngine.")
public class CoreCommands extends ContainerCommand
{
    private final SpongeCore core;

    public CoreCommands(SpongeCore core)
    {
        super(core);
        this.core = core;

        core.getCommandManager().getProviderManager().register(core,
                                                               new FindUserReader());
    }

    @Command(desc = "Reloads the whole CubeEngine")
    public void reload(CommandSender context)
    {
        context.sendTranslated(POSITIVE, "Reloading CubeEngine! This may take some time...");
        final long startTime = System.currentTimeMillis();

        PluginManager pm = core.getGame().getPluginManager();

        context.sendTranslated(POSITIVE, "CubeEngine Reload completed in {integer#time}ms!",
                               System.currentTimeMillis() - startTime);
    }

    @Command(desc = "Reloads all of the modules!")
    public void reloadmodules(CommandSender context, @Flag boolean file)
    {
        context.sendTranslated(POSITIVE, "Reloading all modules! This may take some time...");
        Profiler.startProfiling("modulesReload");
        core.getModulatiry().reloadModules(file);
        long time = Profiler.endProfiling("modulesReload", TimeUnit.SECONDS);
        context.sendTranslated(POSITIVE, "Modules Reload completed in {integer#time}s!", time);
    }



    @Command(desc = "Shows the online mode")
    public void onlinemode(CommandSender context)
    {
        if (this.core.getGame().getServer().getOnlineMode())
        {
            context.sendTranslated(POSITIVE, "The Server is running in online mode");
            return;
        }
        context.sendTranslated(POSITIVE, "The Server is running in offline mode");
        /* Changing online mode is no longer supported on a running server
        BukkitUtils.setOnlineMode(newState);
        if (newState)
        {
            context.sendTranslated(POSITIVE, "The server is now in online-mode.");
        }
        else
        {
            context.sendTranslated(POSITIVE, "The server is not in offline-mode.");
        }
        */
    }

    @Command(desc = "Changes or displays the log level of the server.")
    public void loglevel(CommandSender context, @Optional LogLevel loglevel)
    {
        if (loglevel != null)
        {
            core.getLog().setLevel(loglevel);
            context.sendTranslated(POSITIVE, "New log level successfully set!");
            return;
        }
        context.sendTranslated(NEUTRAL, "The current log level is: {input#loglevel}",
                               core.getLog().getLevel().getName());
    }

    @Command(alias = "finduser", desc = "Searches for a user in the database")
    public void searchuser(CommandContext context,
                           @Reader(FindUserReader.class) @Desc("The name to search for") User name)
    {
        if (name.getName().equalsIgnoreCase(context.getString(0)))
        {
            context.sendTranslated(POSITIVE, "Matched exactly! User: {user}", name);
            return;
        }
        context.sendTranslated(POSITIVE, "Matched not exactly! User: {user}", name);
    }

    public static class FindUserReader implements ArgumentReader<User>
    {
        @Override
        public User read(Class type, CommandInvocation invocation) throws ReaderException
        {
            String name = invocation.consume(1);
            UserManager um = CubeEngine.getCore().getUserManager();
            User found = um.findExactUser(name);
            if (found == null)
            {
                found = um.findUser(name, true);
            }
            if (found == null)
            {
                throw new ReaderException("No match found for {input}!", name);
            }
            return found;
        }
    }
}
