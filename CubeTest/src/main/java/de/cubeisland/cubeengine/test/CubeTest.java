package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder;
import de.cubeisland.cubeengine.core.util.log.DatabaseHandler;
import de.cubeisland.cubeengine.core.util.log.FileHandler;
import de.cubeisland.cubeengine.test.database.TestManager;
import de.cubeisland.cubeengine.test.database.TestModel;
import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CubeTest extends Module
{
    @Override
    public void onEnable()
    {
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
        this.registerEvents(new TestListener(this));
        
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
        TestManager storage = new TestManager(this.getDatabase());
        storage.initialize();
    }

    @Override
    public void onDisable()
    {
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
                .clearTable("test_log").end());//Clears the TestLogs in Database
        }
        catch (Exception e){} //This does fail always with new db

        database.preparedExecute(TestModel.class, "store", this.getDate(2012, 8, 8), 10, "Heinz");
        database.preparedExecute(TestModel.class, "store", this.getDate(2012, 6, 8), 30, "Hans");
        database.preparedExecute(TestModel.class, "store", this.getDate(2012, 8, 6), 20, "Manfred");
        database.preparedExecute(TestModel.class, "store", this.getDate(2012, 8, 8), 20, "Heinz");
        database.preparedExecute(TestModel.class, "store", this.getDate(2012, 8, 8), 120, "Hans");
        database.preparedExecute(TestModel.class, "store", this.getDate(2011, 2, 8), 50, "Manfred");

        database.preparedQuery(TestModel.class, "get", 2);
        database.preparedQuery(TestModel.class, "getall");
        database.preparedExecute(TestModel.class, "update", this.getDate(111, 2, 2), 100, "Paul", 3);
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
                .is(ComponentBuilder.GREATER)
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
                .is(ComponentBuilder.GREATER)
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
        //TODO
    }
}
