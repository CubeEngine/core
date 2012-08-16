package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.AttrType;
import de.cubeisland.cubeengine.core.persistence.SQLBuilder;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
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
        SQLBuilder sqlb = new SQLBuilder(this.getDatabase());
        System.out.println("#########################################################");
        System.out.println(
            sqlb.createTableINE("users")
                .attribute("id", AttrType.INT, 11).unsigned().notNull().autoincrement().next()
                .attribute("name", AttrType.VARCHAR, 16).notNull().next()
                .attribute("lang", AttrType.VARCHAR, 10).notNull().next()
                .primaryKey("id")
                .engine("MyISAM").defaultcharset("latin1").autoincrement(1)
                );
        
        System.out.println(
            sqlb.select("id","item").from("users").where("id").limit(1)
                );
    }
}
