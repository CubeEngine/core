package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.FunctionBuilder;
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

        this.getDatabase().execute(this.getDatabase().getQueryBuilder().dropTable("Orders").endQuery());
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
                                .endFunctions()
                            .from("Orders")
                            .endBuilder()
                        .endQuery());
        
        database.query(
                database.getQueryBuilder()
                        .select().cols("id","Customer")
                            .beginFunctions()
                                .comma()
                                .beginFunction("sum")
                                    .field("OrderPrice")
                                .endFunction()
                                .as("OrderAverage")
                            .endFunctions()
                            .from("Orders")
                            .beginFunctions()
                                .groupBy("Customer")
                                .having()
                                .beginFunction("sum")
                                    .field("OrderPrice")
                                .endFunction()
                                .is(FunctionBuilder.GREATER)
                                .value("100")
                            .endFunctions()
                        .endBuilder()
                    .endQuery());

        //SELECT ROUND(AVG(*)) FROM `table` WHERE `dob_year`>1920
        database.getQueryBuilder()
                .select()
                    .beginFunction("round")
                        .beginFunction("avg")
                            .wildcard()
                        .endFunction()
                    .endFunction().endFunctions()
                    .from("table")
                    .beginFunction("where")
                        .field("dob_year")
                        .is(FunctionBuilder.GREATER)
                        .value("1920")
                    .endFunction().endFunctions()
                .endBuilder()
            .endQuery();

        //SELECT ProductName, ROUND(UnitPrice,0) as UnitPrice FROM Products
        database.getQueryBuilder()
                .select()
                    .cols("ProductName")
                    .beginFunctions()
                        .comma()
                        .beginFunction("round")
                            .field("UnitPrice")
                            .comma().value("0")
                        .endFunction()
                        .as("UnitPrice")
                    .endFunctions()
                    .from("Products")
                .endBuilder()
            .endQuery();
        
        //SELECT LCASE(LastName) as LastName,FirstName FROM Persons
        database.getQueryBuilder()
                .select()
                    .beginFunction("lcase")
                        .field("LastName")
                    .endFunction()
                    .as("LastName")
                    .comma()
                    .field("FirstName")
                    .endFunctions()
                    .from("Persons")
                .endBuilder()
            .endQuery();
        
    }

    public void testl18n()
    {
        //TODO
    }
}
