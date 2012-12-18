package de.cubeisland.cubeengine.shout;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.shout.announce.AnnouncementManager;
import de.cubeisland.cubeengine.shout.announce.announcer.Announcer;
import de.cubeisland.cubeengine.shout.interactions.ShoutCommand;
import de.cubeisland.cubeengine.shout.interactions.ShoutListener;
import de.cubeisland.cubeengine.shout.interactions.ShoutSubCommands;
import java.io.File;
import java.io.IOException;

public class Shout extends Module
{
    private AnnouncementManager announcementManager;
    private ShoutListener listener;
    private ShoutCommand command;
    private ShoutSubCommands subCommands;
    private Announcer announcer;
    private ShoutConfiguration config;
    private File announcementFolder;

    // TODO CubeRoles
    @Override
    public void onEnable()
    {
        boolean firstRun = true;
        File file = new File(this.getFolder(), ".shout");
        if (file.exists())
        {
            firstRun = false;
        }
        try
        {
            file.createNewFile();
        }
        catch (IOException ex)
        {
            if (this.getCore().isDebug())
            {
                this.getLogger().log(LogLevel.WARNING, "There was an error creating a file!", ex);
            }
        }

        this.announcementFolder = this.getFolder();
        this.getFileManager().dropResources(ShoutResource.values());

        this.announcer = new Announcer(config.initDelay);
        this.announcementManager = new AnnouncementManager(this, announcementFolder);
        this.listener = new ShoutListener(this);
        this.command = new ShoutCommand(this);
        this.subCommands = new ShoutSubCommands(this);

        if (firstRun)
        {
            try
            {
                this.announcementManager.createAnnouncement("Example", this.getCore().getConfiguration().defaultLanguage,
                        "This is an example announcement", "10 minutes", "*", "*", "*");
            }
            catch (Exception ex)
            {
                this.getLogger().log(LogLevel.WARNING, "An exception occured when creating the example announcement");
                this.getLogger().log(LogLevel.WARNING, "The message was: " + ex.getLocalizedMessage());
                this.getLogger().log(LogLevel.DEBUG, ex.getLocalizedMessage(), ex);
            }
        }
        this.announcementManager.loadAnnouncements(this.announcementFolder);
        this.registerListener(listener);
        this.registerCommands(command);
        this.registerCommands(subCommands, "shout");

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
        return announcer;
    }
}
