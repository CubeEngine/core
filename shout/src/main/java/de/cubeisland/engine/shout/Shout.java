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

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.shout.announce.AnnouncementManager;
import de.cubeisland.engine.shout.announce.announcer.Announcer;
import de.cubeisland.engine.shout.interactions.ShoutCommand;
import de.cubeisland.engine.shout.interactions.ShoutListener;

public class Shout extends Module
{
    private AnnouncementManager announcementManager;
    private Announcer announcer;
    private ShoutConfiguration config;

    private Permission announcePerm;

    public Permission getAnnouncePerm()
    {
        return announcePerm;
    }

    @Override
    public void onEnable()
    {
        this.announcePerm = this.getBasePermission().newWildcard("announcement");

        this.config = this.loadConfig(ShoutConfiguration.class);

        this.announcer = new Announcer(this.getCore().getTaskManager().getThreadFactory(this), this.config.initialDelay);
        this.announcementManager = new AnnouncementManager(this, this.getFolder());

        if (isFirstRun())
        {
            try
            {
                this.announcementManager.createAnnouncement("Example", this.getCore().getConfiguration().defaultLocale,
                        "This is an example announcement", "10 minutes", "*", "*", false);
            }
            catch (Exception ex)
            {
                this.getLog().warn(ex, "An exception occured when creating the example announcement!");
            }
        }
        this.announcementManager.loadAnnouncements(this.getFolder());
        this.getCore().getEventManager().registerListener(this, new ShoutListener(this));
        this.getCore().getCommandManager().registerCommand(new ShoutCommand(this));

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
        catch (IOException ex)
        {
            this.getLog().debug(ex, "There was an error creating a file: {}", file);
        }
        return true;
    }
}
