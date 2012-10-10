package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.PlayerLanguageReceivedEvent;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.From;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.database.Database;
import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.*;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.EnchantMatcher;
import de.cubeisland.cubeengine.core.util.EntityMatcher;
import de.cubeisland.cubeengine.core.util.MaterialMatcher;
import de.cubeisland.cubeengine.core.util.log.FileHandler;
import de.cubeisland.cubeengine.test.database.TestManager;
import de.cubeisland.cubeengine.test.database.TestModel;
import de.cubeisland.cubeengine.test.l18n.TestRecource;
import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Test extends Module
{
    public TestManager manager;
    public UserManager uM;
    @From
    protected TestConfig config;
    public static List<String> aListOfPlayers;
    
    public Basics basicsModule;

    @Override
    public void onEnable()
    {
        Configuration.load(TestConfig2.class, new File(this.getFolder(), "updateConfig.yml"));
        this.getFileManager().dropResources(TestRecource.values());
        this.uM = this.getUserManager();
        try
        {
            this.initializeDatabase();
            this.testDatabase();
        }
        catch (Exception ex)
        {
            this.getLogger().log(Level.SEVERE, "Error while Enabling the TestModule", ex);
        }
        try
        {
            this.getLogger().addHandler(new FileHandler(Level.ALL, new File(this.getFileManager().getLogDir(), "test").toString()));
        }
        catch (Exception ex)
        {
            this.getLogger().log(Level.SEVERE, "Error while adding the FileHandler", ex);
        }
        this.registerListener(new TestListener(this));

        this.testUserManager();
        this.testl18n();
        this.testMatchers();
        this.testsomeUtils();

        this.registerListener(new Listener() {
            @EventHandler
            public void onLanguageReceived(PlayerLanguageReceivedEvent event)
            {
                System.out.print("Player: " + event.getPlayer().getName() + " Lang: " + event.getLanguage());
            }
        });
        
        this.getLogger().info("Basics-Module: " + String.valueOf(basicsModule));
    }

    public void initializeDatabase() throws SQLException
    {
        try
        {
            this.getDatabase().execute(this.getDatabase().getQueryBuilder().dropTable("Orders").end());
        }
        catch (Exception e)
        {
        }
        manager = new TestManager(this.getDatabase());

    }

    @Override
    public void onDisable()
    {
    }

    public void testUserManager()
    {
        //Testing get
        User userToDel = uM.getUser("userGetsDel");
        User user = uM.getUser("UserU");
        uM.getUser("User1");
        uM.getUser("User2");
        //Testing getall
        uM.getAll();
        //Testing delete
        uM.delete(userToDel);
        //Test update
        user.nogc = true;
        user.lastseen = new Timestamp(50000);
        uM.update(user);
        user = uM.getUser("User1");
        user.lastseen = new Timestamp(50000);
        uM.update(user);
    }

    private Date getDate(int year, int month, int day)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return new Date(calendar.getTimeInMillis());
    }

    public void testDatabase() throws SQLException
    {
        Database database = this.getDatabase();

        try
        {
            database.execute(database.getQueryBuilder()
                    .clearTable("test_log").end());//Clears the TestLogs in Database (This does always fail with new db)
        }
        catch (Exception e)
        {
        }

        this.manager.store(new TestModel(this.getDate(2012, 8, 8), 10, "Heinz"));
        this.manager.store(new TestModel(this.getDate(2012, 6, 8), 30, "Hans"));
        this.manager.store(new TestModel(this.getDate(2012, 8, 6), 20, "Manfred"));
        this.manager.store(new TestModel(this.getDate(2012, 8, 8), 20, "Heinz"));
        this.manager.store(new TestModel(this.getDate(2012, 8, 8), 120, "Hans"));
        this.manager.store(new TestModel(this.getDate(2011, 2, 8), 50, "Manfred"));
        this.manager.get(2);
        this.manager.getAll();
        TestModel model = this.manager.get(3);
        model.orderDate = this.getDate(111, 2, 2);
        model.orderPrice = 100;
        model.customer = "Paul";
        this.manager.update(model);

        database.query(
                database.getQueryBuilder()
                .select()
                .beginFunction("AVG")
                .field("OrderPrice")
                .endFunction()
                .as("OrderAverage")
                .from("Orders")
                .end()
                .end());

        database.query(
                database.getQueryBuilder()
                .select().cols("id", "Customer")
                .rawSQL(",")
                .beginFunction("SUM")
                .field("OrderPrice")
                .endFunction()
                .as("OrderAverage")
                .from("Orders")
                .groupBy("Customer")
                .having()
                .beginFunction("sum")
                .field("OrderPrice")
                .endFunction()
                .is(GREATER)
                .value(100)
                .end()
                .end());

        //SELECT ROUND(AVG(*)) FROM `table` WHERE `dob_year`>1920
        database.getQueryBuilder()
                .select()
                .beginFunction("round")
                .beginFunction("avg")
                .wildcard()
                .endFunction()
                .endFunction()
                .from("table")
                .beginFunction("where")
                .field("dob_year")
                .is(GREATER)
                .value("1920")
                .endFunction()
                .end()
                .end();

        //SELECT ProductName, ROUND(UnitPrice,0) as UnitPrice FROM Products
        database.getQueryBuilder()
                .select()
                .cols("ProductName")
                .rawSQL(",")
                .beginFunction("round")
                .field("UnitPrice")
                .rawSQL(",").value("0")
                .endFunction()
                .as("UnitPrice")
                .from("Products")
                .end()
                .end();

        //SELECT LCASE(LastName) as LastName,FirstName FROM Persons
        database.getQueryBuilder()
                .select()
                .beginFunction("lcase")
                .field("LastName")
                .endFunction()
                .as("LastName")
                .rawSQL(",")
                .field("FirstName")
                .from("Persons")
                .end()
                .end();
    }

    public void testl18n()
    {
        this.getLogger().debug(CubeEngine.getCore().getI18n().translate("de_DE", "test", "english TEST"));
        this.getLogger().debug(CubeEngine.getCore().getI18n().translate("fr_FR", "test", "english TEST"));
    }

    private void testMatchers()
    {
        this.getLogger().debug(EnchantMatcher.get().matchEnchantment("infinity"));
        this.getLogger().debug(EnchantMatcher.get().matchEnchantment("infini"));
        this.getLogger().debug(EnchantMatcher.get().matchEnchantment("hablablubb") + " is null");
        this.getLogger().debug(EnchantMatcher.get().matchEnchantment("protect"));
        this.getLogger().debug(MaterialMatcher.get().matchItemStack("stone").serialize());
        this.getLogger().debug(MaterialMatcher.get().matchItemStack("stoned").serialize());
        this.getLogger().debug(MaterialMatcher.get().matchItemStack("hablablubb") + " is null");
        this.getLogger().debug(MaterialMatcher.get().matchItemStack("wool:red").serialize());
        this.getLogger().debug(MaterialMatcher.get().matchItemStack("35").serialize());
        this.getLogger().debug(MaterialMatcher.get().matchItemStack("35:15").serialize());
        this.getLogger().debug(MaterialMatcher.get().matchItemStack("35:red").serialize());
        this.getLogger().debug(MaterialMatcher.get().matchItemStack("wood:birch").serialize());
        this.getLogger().debug(MaterialMatcher.get().matchItemStack("leves:pine").serialize());
        this.getLogger().debug(MaterialMatcher.get().matchItemStack("spawnegg:pig").serialize());
        this.getLogger().debug(EntityMatcher.get().matchEntity("pig"));
        this.getLogger().debug(EntityMatcher.get().matchMonster("zombi"));
        this.getLogger().debug(EntityMatcher.get().matchFriendlyMob("shep"));
        this.getLogger().debug(EntityMatcher.get().matchFriendlyMob("ghast") + " is null");
    }

    private void testsomeUtils()
    {
        try
        {
            aListOfPlayers = FileUtil.readStringList(new File(this.getFolder(), "testdata/player.txt"));
        }
        catch (Exception ex)
        {
            this.getLogger().log(Level.SEVERE, "Error in testsomeutils", ex);
        }
    }
}
