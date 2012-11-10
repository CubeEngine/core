package de.cubeisland.cubeengine.shout;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.shout.interactions.ShoutCommand;
import de.cubeisland.cubeengine.shout.interactions.ShoutListener;
import de.cubeisland.cubeengine.shout.interactions.ShoutSubCommands;
import de.cubeisland.cubeengine.shout.task.AnnouncementManager;
import de.cubeisland.cubeengine.shout.task.Announcer;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Shout extends Module
{
    private AnnouncementManager announcementManager;
    private ShoutListener listener;
    private ShoutCommand command;
    private ShoutSubCommands subCommands;
    private Announcer taskManager;
    private ShoutConfiguration config;
    public File announcementFolder;

    // TODO CubeRoles
    @Override
    public void onEnable()
    {
        boolean firstRun = true;
        File f = new File(this.getFolder(), ".shout");
        if (f.exists())
        {
            firstRun = false;
        }
        try
        {
            f.createNewFile();
        }
        catch (IOException ex)
        {
            if (this.getCore().isDebug())
            {
                this.getLogger().log(Level.WARNING, "There was an error creating a file!", ex);
            }
        }
        
        this.announcementFolder = this.getFolder();
        this.getFileManager().dropResources(ShoutResource.values());

        this.taskManager = new Announcer(config.initDelay);
        this.announcementManager = new AnnouncementManager(this);
        this.listener = new ShoutListener(this);
        this.command = new ShoutCommand(this);
        this.subCommands = new ShoutSubCommands(this);

        if (firstRun)
        {
            try
            {
                this.announcementManager.createAnnouncement("Example", "This is an example announcement",
                    "10 minutes", "*", "*", "*", this.getCore().getConfiguration().defaultLanguage);
            }
            catch (Exception ex)
            {
                this.getLogger().log(Level.WARNING, "An exception occured when creating the example announcement");
                this.getLogger().log(Level.WARNING, "The message was: " + ex.getLocalizedMessage());
                if (this.getCore().getConfiguration().debugMode)
                {
                    this.getLogger().log(Level.WARNING, null, ex);
                }
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
        this.taskManager.stopAll();
    }

    public AnnouncementManager getAnnouncementManager()
    {
        return this.announcementManager;
    }

    public Announcer getTaskManager()
    {
        return taskManager;
    }
}