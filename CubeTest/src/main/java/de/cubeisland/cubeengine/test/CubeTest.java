package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder;
import de.cubeisland.cubeengine.test.database.TestModel;
import de.cubeisland.cubeengine.test.database.TestStorage;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Level;

public class CubeTest extends Module
{
    @Override
    public void onEnable()
    {
        try
        {
            this.getLogger().info("enabling TestModule");
            Configuration.load(TestConfig.class, this);
            this.initializeDatabase();
            this.testDatabase();
        }
        catch (Exception ex)
        {
            this.getLogger().log(Level.SEVERE, "Error while Enabling the TestModule", ex);
        }
        this.getLogger().info("TestModule succesfully enabeled");

    }

    public void initializeDatabase() throws SQLException
    {

        this.getDatabase().execute(this.getDatabase().getQueryBuilder().dropTable("Orders").end());
        TestStorage storage = new TestStorage(this.getDatabase());
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
        database.preparedExecute(TestModel.class, "store", this.getDate(2012, 8, 8), 10, "Heinz");
        database.preparedExecute(TestModel.class, "store", this.getDate(2012, 6, 8), 30, "Hans");
        database.preparedExecute(TestModel.class, "store", this.getDate(2012, 8, 6), 20, "Manfred");
        database.preparedExecute(TestModel.class, "store", this.getDate(2012, 8, 8), 20, "Heinz");
        database.preparedExecute(TestModel.class, "store", this.getDate(2012, 8, 8), 120, "Hans");
        database.preparedExecute(TestModel.class, "store", this.getDate(2011, 2, 8), 50, "Manfred");
        
        database.preparedQuery(TestModel.class, "get", 2);
        database.preparedQuery(TestModel.class, "getall");
        database.preparedExecute(TestModel.class, "update", this.getDate(111, 2, 2) , 100 , "Paul", 3);
        database.query(
                database.getQueryBuilder()
                            .select()
                                .beginFunction("avg")
                                    .field("OrderPrice")
                                .endFunction()
                                .as("OrderAverage")
                            .from("Orders")
                            .end()
                        .end());
        
        database.query(
                database.getQueryBuilder()
                        .select().cols("id","Customer")
                                .rawSQL(",")
                                .beginFunction("sum")
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
                                .value("100")
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
