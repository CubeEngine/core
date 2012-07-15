package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.event.EventManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import de.cubeisland.cubeengine.core.user.UserManager;
import java.util.logging.Logger;

/**
 *
 * @author Phillip Schichtel
 */
public interface Core
{
    public void enable();
    public void disable();
    
    public Database getDatabase();
    public PermissionRegistration getPermissionRegistration();
    public EventManager getEventManager();
    public UserManager getUserManager();
    public FileManager getFileManager();
    public Logger getLogger();
    public ModuleManager getModuleManager();
    public I18n getI18n();
    public CoreConfiguration getConfiguration();
    public Bootstrapper getBootstrapper();
    public CommandManager getCommandManager();
}
