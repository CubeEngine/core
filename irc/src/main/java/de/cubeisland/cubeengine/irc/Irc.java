package de.cubeisland.cubeengine.irc;

import de.cubeisland.cubeengine.core.module.Module;

/**
 * Represents a Irc
 */
public class Irc extends Module
{
    private IrcConfig config;
    private BotManager mgr;

    @Override
    public void onEnable()
    {
    //        this.mgr = new BotManager(this.config);
    //        this.registerListener(new IrcListener(this));
    //        this.mgr.connect();
    //        this.registerCommands(new Test(this));
    //        this.getFileManager().dropResources(Test.values());
    //        this.registerPermissions(Test.values());
    //        this.flyManager = new FlyManager(this.getDatabase(), this.getInfo().getRevision());
    }

    @Override
    public void onDisable()
    {
    //        this.mgr.clean();
    //        this.mgr = null;
    }

    public BotManager getBotManager()
    {
        return this.mgr;
    }
}
