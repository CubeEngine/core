package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.database.Database;
import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.*;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.log.DatabaseHandler;
import de.cubeisland.cubeengine.core.util.log.FileHandler;
import de.cubeisland.cubeengine.test.database.TestManager;
import de.cubeisland.cubeengine.test.database.TestModel;
import de.cubeisland.cubeengine.test.l18n.TestRecource;
import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Test extends Module
{
    public TestManager manager;
    public UserManager uM;

    @Override
    public void onEnable()
    {
        this.uM = this.getUserManager();
        Logger logger = this.getLogger();
        try
        {
            logger.info("enabling TestModule");
            Configuration.load(TestConfig.class, this);
            this.initializeDatabase();
            this.testDatabase();
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Error while Enabling the TestModule", ex);
        }
        logger.addHandler(new DatabaseHandler(Level.WARNING, this.getDatabase(), "test_log"));
        try
        {
            logger.addHandler(new FileHandler(Level.ALL, new File(this.getFileManager().getLogDir(), "test.log").toString()));
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Error while adding the FileHandler", ex);
        }
        logger.severe("SevereTestLog");
        logger.warning("WarningTestLog");
        this.getCore().getEventManager().registerListener(new TestListener(this), this);

        this.testUserManager();
        this.testl18n();

        logger.info("TestModule succesfully enabeled");
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
        manager.initialize();
    }

    @Override
    public void onDisable()
    {
    }

    public void testUserManager()
    {
        //Testing insert
        User user = uM.getUser("FakeUser");
        //Testing delete
        uM.delete(user);
        //Testing get
        uM.getUser("FakerXL");
        uM.getUser("NoPlayer");
        uM.getUser("NoUserAtAll");
        user = uM.getUser("NoUser");
        //Testung update
        user.setLanguage("de");
        uM.update(user);
        //Testing getall
        uM.getAll();
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
        CubeEngine.getFileManager().dropResources(TestRecource.values());//TODO this does not get the right Resource!
        System.out.println(CubeEngine.getCore().getI18n().translate("de_DE", "test", "english TEST"));
    }
}
