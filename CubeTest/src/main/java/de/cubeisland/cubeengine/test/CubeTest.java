package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.test.database.TestStorage;
import java.sql.SQLException;

public class CubeTest extends Module
{
    @Override
    public void onEnable()
    {
        this.getLogger().info("Test1 onEnable...");
        Configuration.load(TestConfig.class, this);
        this.initializeDatabase();
        this.testDatabase();

    }

    public void initializeDatabase()
    {
        try
        {
            TestStorage storage = new TestStorage(this.getDatabase());
            storage.initialize();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable()
    {
    }

    public void testDatabase()
    {
        this.getDatabase().buildQuery().initialize();//TODO test here
    }
    
    public void testl18n()
    {
        //TODO
    }
}
