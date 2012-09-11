package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.core.module.Module;
import java.io.File;
import java.util.logging.Logger;

public class Conomy extends Module
{
    protected static Logger logger = null;
    public static boolean debugMode = false;
    
    protected File dataFolder;
    private static final String PERMISSION_BASE = "cubewar.conomy";

    @Override
    public void onEnable()
    {
        logger = this.getLogger();
        
        //Configuration configuration = this.getConfig();
        //configuration.options().copyDefaults(true);
        //debugMode = configuration.getBoolean("debug");
        
               
        //CubeCore.getInstance().getPermissionRegistration().registerPermissions(Perm.values());

    }

    @Override
    public void onDisable()
    {
    }
}
