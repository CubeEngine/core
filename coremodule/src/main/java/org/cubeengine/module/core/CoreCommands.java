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
import org.cubeengine.butler.parametric.Command;
import org.cubeengine.butler.parametric.Desc;
import org.cubeengine.butler.parametric.Flag;
import org.cubeengine.butler.parametric.Optional;
import org.cubeengine.butler.parametric.Reader;
import de.cubeisland.engine.logscribe.LogLevel;
import de.cubeisland.engine.modularity.core.Modularity;
import org.cubeengine.service.command.CommandContext;
import org.cubeengine.service.command.ContainerCommand;
import org.cubeengine.service.command.readers.FindUserReader;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.module.core.util.Profiler;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.command.CommandSource;

import static org.cubeengine.service.i18n.formatter.MessageType.NEUTRAL;
import static org.cubeengine.service.i18n.formatter.MessageType.POSITIVE;

@Command(name = "cubeengine", alias = "ce",
    desc = "These are the basic commands of the CubeEngine.")
public class CoreCommands extends ContainerCommand
{
    private final CoreModule core;
    private I18n i18n;

    public CoreCommands(CoreModule core, I18n i18n)
    {
        super(core);
        this.core = core;
        this.i18n = i18n;
    }

    @Command(desc = "Reloads the whole CubeEngine")
    public void reload(CommandSource context)
    {
        // TODO move all of reload to Plugin instead of coremodule
        i18n.sendTranslated(context, POSITIVE, "Reloading CubeEngine! This may take some time...");
        final long startTime = System.currentTimeMillis();

        PluginManager pm = core.getGame().getPluginManager();

        i18n.sendTranslated(context, POSITIVE, "CubeEngine Reload completed in {integer#time}ms!",
                               System.currentTimeMillis() - startTime);
    }

    @Command(desc = "Reloads all of the modules!")
    public void reloadmodules(CommandSource context, @Flag boolean file)
    {
        i18n.sendTranslated(context, POSITIVE, "Reloading all modules! This may take some time...");
        Profiler.startProfiling("modulesReload");
        Modularity modulatiry = core.getModularity();
        modulatiry.getGraph().getRoot();
        long time = Profiler.endProfiling("modulesReload", TimeUnit.SECONDS);
        i18n.sendTranslated(context, POSITIVE, "Modules Reload completed in {integer#time}s!", time);
    }



    @Command(desc = "Shows the online mode")
    public void onlinemode(CommandSource context)
    {
        if (this.core.getGame().getServer().getOnlineMode())
        {
            i18n.sendTranslated(context, POSITIVE, "The Server is running in online mode");
            return;
        }
        i18n.sendTranslated(context, POSITIVE, "The Server is running in offline mode");
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
    public void loglevel(CommandSource context, @Optional LogLevel loglevel)
    {
        if (loglevel != null)
        {
            core.getLog().setLevel(loglevel);
            i18n.sendTranslated(context, POSITIVE, "New log level successfully set!");
            return;
        }
        i18n.sendTranslated(context, NEUTRAL, "The current log level is: {input#loglevel}",
                               core.getLog().getLevel().getName());
    }

    @Command(alias = "finduser", desc = "Searches for a user in the database")
    public void searchuser(CommandContext context, @Reader(FindUserReader.class) @Desc("The name to search for") User name)
    {
        if (name.getName().equalsIgnoreCase(context.getString(0)))
        {
            context.sendTranslated(POSITIVE, "Matched exactly! User: {user}", name);
            return;
        }
        context.sendTranslated(POSITIVE, "Matched not exactly! User: {user}", name);
    }
}
