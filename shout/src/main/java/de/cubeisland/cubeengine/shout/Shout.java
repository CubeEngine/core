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
package de.cubeisland.cubeengine.shout;

import java.io.File;
import java.io.IOException;

import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.module.Inject;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.shout.announce.AnnouncementManager;
import de.cubeisland.cubeengine.shout.announce.announcer.Announcer;
import de.cubeisland.cubeengine.shout.interactions.ShoutCommand;
import de.cubeisland.cubeengine.shout.interactions.ShoutListener;
import de.cubeisland.cubeengine.shout.interactions.ShoutSubCommands;

public class Shout extends Module
{
    @Inject private Roles roles;
    public boolean usingRoles = false;

    private AnnouncementManager announcementManager;
    private Announcer announcer;
    private ShoutConfiguration config;
    private File announcementFolder;

    @Override
    public void onEnable()
    {
        this.config = Configuration.load(ShoutConfiguration.class, this);
        this.announcementFolder = this.getFolder();
        // this.getCore().getFileManager().dropResources(ShoutResource.values());

        if (this.getCore().getModuleManager().getModule(Roles.class) == null)
        {
            this.usingRoles = true;
            this.roles = this.getCore().getModuleManager().getModule(Roles.class);
        }

        this.announcer = new Announcer(this.getCore().getTaskManager(), this.config.initDelay);
        this.announcementManager = new AnnouncementManager(this, this.announcementFolder);

        if (isFirstRun())
        {
            try
            {
                this.announcementManager.createAnnouncement("Example", this.getCore().getConfiguration().defaultLocale,
                        "This is an example announcement", "10 minutes", "*", "*", "*", false);
            }
            catch (Exception ex)
            {
                this.getLog().warn("An exception occured when creating the example announcement");
                this.getLog().warn("The message was: " + ex.getLocalizedMessage());
                this.getLog().debug(ex.getLocalizedMessage(), ex);
            }
        }
        this.announcementManager.loadAnnouncements(this.announcementFolder);
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
        File file = new File(this.getFolder(), ".shout");
        if (file.exists())
        {
            return false;
        }
        try
        {
            file.createNewFile();
            return true;
        }
        catch (IOException ex)
        {
            if (this.getCore().isDebug())
            {
                this.getLog().warn("There was an error creating a file!", ex);
            }
            return false;
        }
    }
}
