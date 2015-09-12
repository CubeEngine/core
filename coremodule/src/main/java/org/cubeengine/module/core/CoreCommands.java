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
package org.cubeengine.module.core;

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
import de.cubeisland.engine.modularity.core.Modularity;
import org.cubeengine.service.command.CommandContext;
import org.cubeengine.service.command.CommandManager;
import org.cubeengine.service.command.ContainerCommand;
import org.cubeengine.module.core.sponge.CoreModule;
import org.cubeengine.service.command.Multilingual;
import org.cubeengine.service.user.MultilingualPlayer;
import org.cubeengine.service.user.UserManager;
import org.cubeengine.module.core.util.Profiler;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.plugin.PluginManager;

import static org.cubeengine.service.i18n.formatter.MessageType.NEUTRAL;
import static org.cubeengine.service.i18n.formatter.MessageType.POSITIVE;

@Command(name = "cubeengine", alias = "ce",
    desc = "These are the basic commands of the CubeEngine.")
public class CoreCommands extends ContainerCommand
{
    private final CoreModule core;

    public CoreCommands(CoreModule core, CommandManager cm, UserManager um)
    {
        super(core);
        this.core = core;
        cm.getProviderManager().register(core, new FindUserReader(um));
    }

    @Command(desc = "Reloads the whole CubeEngine")
    public void reload(Multilingual context)
    {
        // TODO move all of reload to Plugin
        context.sendTranslated(POSITIVE, "Reloading CubeEngine! This may take some time...");
        final long startTime = System.currentTimeMillis();

        PluginManager pm = core.getGame().getPluginManager();

        context.sendTranslated(POSITIVE, "CubeEngine Reload completed in {integer#time}ms!",
                               System.currentTimeMillis() - startTime);
    }

    @Command(desc = "Reloads all of the modules!")
    public void reloadmodules(Multilingual context, @Flag boolean file)
    {
        context.sendTranslated(POSITIVE, "Reloading all modules! This may take some time...");
        Profiler.startProfiling("modulesReload");
        Modularity modulatiry = core.getModularity();
        modulatiry.getGraph().getRoot();
        long time = Profiler.endProfiling("modulesReload", TimeUnit.SECONDS);
        context.sendTranslated(POSITIVE, "Modules Reload completed in {integer#time}s!", time);
    }



    @Command(desc = "Shows the online mode")
    public void onlinemode(Multilingual context)
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
    public void loglevel(Multilingual context, @Optional LogLevel loglevel)
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
        private final UserManager um;

        public FindUserReader(UserManager um)
        {
            this.um = um;
        }

        @Override
        public User read(Class type, CommandInvocation invocation) throws ReaderException
        {
            String name = invocation.consume(1);
            com.google.common.base.Optional<User> found = um.getByName(name);
            if (!found.isPresent())
            {
                found = com.google.common.base.Optional.fromNullable(um.findUser(name, true));
            }
            if (found == null)
            {
                throw new ReaderException("No match found for {input}!", name);
            }
            return found.get();
        }
    }
}
