package de.cubeisland.cubeengine.shout;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.cubeisland.cubeengine.core.config.annotations.From;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.shout.interactions.ShoutCommand;
import de.cubeisland.cubeengine.shout.interactions.ShoutListener;
import de.cubeisland.cubeengine.shout.interactions.ShoutSubCommands;
import de.cubeisland.cubeengine.shout.task.AnnouncementManager;
import de.cubeisland.cubeengine.shout.task.TaskManager;

public class Shout extends Module
{
    private AnnouncementManager announcementManager;
    private ShoutListener listener;
    private ShoutCommand command;
    private ShoutSubCommands subCommands;
    private TaskManager taskManager;
    @From
    private ShoutConfiguration config;
    public Logger logger;
    public File announcementFolder;
    public boolean firstRun;

    // TODO CubeRoles
    @Override
    public void onEnable()
    {
        File f = new File(this.getFolder(), ".shout");
        if (f.exists())
        {
            this.firstRun = false;
        }
        try
        {
            f.createNewFile();
        }
        catch (IOException ex)
        {
            this.logger.log(Level.WARNING, "There was an error creating a file");
            this.logger.log(Level.WARNING, "The error message was: " + ex.getLocalizedMessage());
            if (this.getCore().isDebug())
            {
                ex.printStackTrace();
            }
        }
        this.firstRun = true;


        this.logger = this.getLogger();
        this.announcementFolder = this.getFolder();
        this.getFileManager().dropResources(ShoutResource.values());

        this.taskManager = new TaskManager(this, config.initDelay, config.messagerPeriod);
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
                this.logger.log(Level.WARNING, "An exception occured when creating the example announcement");
                this.logger.log(Level.WARNING, "The message was: " + ex.getLocalizedMessage());
                if (this.getCore().getConfiguration().debugMode)
                {
                    this.logger.log(Level.WARNING, null, ex);
                }
            }
        }

        this.announcementManager.loadAnnouncements(this.announcementFolder);
        this.registerListener(listener);
        this.registerCommands(command);
        this.registerCommands(subCommands, "shout");

    }

    @Override
    public void onDisable()
    {
    }

    public AnnouncementManager getAnnouncementManager()
    {
        return this.announcementManager;
    }

    public TaskManager getTaskManager()
    {
        return taskManager;
    }
}
