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
package de.cubeisland.engine.shout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.config.Configuration;

import de.cubeisland.engine.core.module.Inject;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.shout.announce.AnnouncementManager;
import de.cubeisland.engine.shout.announce.announcer.Announcer;
import de.cubeisland.engine.shout.interactions.ShoutCommand;
import de.cubeisland.engine.shout.interactions.ShoutListener;
import de.cubeisland.engine.shout.interactions.ShoutSubCommands;

public class Shout extends Module
{
    @Inject private Roles roles;
    public boolean usingRoles = false;

    private AnnouncementManager announcementManager;
    private Announcer announcer;
    private ShoutConfiguration config;

    @Override
    public void onEnable()
    {
        this.config = Configuration.load(ShoutConfiguration.class, this);
        // this.getCore().getFileManager().dropResources(ShoutResource.values());

        if (this.getCore().getModuleManager().getModule(Roles.class) == null)
        {
            this.usingRoles = true;
            this.roles = this.getCore().getModuleManager().getModule(Roles.class);
        }

        this.announcer = new Announcer(this.getCore().getTaskManager(), this.config.initDelay);
        this.announcementManager = new AnnouncementManager(this, this.getFolder());

        if (isFirstRun())
        {
            try
            {
                this.announcementManager.createAnnouncement("Example", this.getCore().getConfiguration().defaultLocale,
                        "This is an example announcement", "10 minutes", "*", "*", "*", false);
            }
            catch (Exception ex)
            {
                this.getLog().warn("An exception occured when creating the example announcement: " +
                                       ex.getLocalizedMessage(), ex);
            }
        }
        this.announcementManager.loadAnnouncements(this.getFolder());
        this.getCore().getEventManager().registerListener(this, new ShoutListener(this));
        this.getCore().getCommandManager().registerCommands(this, new ShoutCommand(this), ReflectedCommand.class);
        this.getCore().getCommandManager().registerCommands(this, new ShoutSubCommands(this), ReflectedCommand.class, "shout");

        this.announcementManager.initUsers();
    }

    @Override
    public void onDisable()
    {
        this.announcer.shutdown();
    }

    public AnnouncementManager getAnnouncementManager()
    {
        return this.announcementManager;
    }

    public Announcer getAnnouncer()
    {
        return this.announcer;
    }

    private boolean isFirstRun()
    {
        Path file = this.getFolder().resolve(".shout");
        if (Files.exists(file))
        {
            return false;
        }
        try
        {
            Files.createFile(file);
        }
        catch (IOException e)
        {
            this.getLog().debug("There was an error creating a file: {}", file);
            this.getLog().debug(e.getLocalizedMessage(), e);
        }
        return true;
    }
}
