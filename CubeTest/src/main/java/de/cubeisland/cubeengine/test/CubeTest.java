package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.FunctionBuilder;
import de.cubeisland.cubeengine.test.database.TestModel;
import de.cubeisland.cubeengine.test.database.TestStorage;
import java.sql.Date;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CubeTest extends Module
{
    @Override
    public void onEnable()
    {
        try
        {
            this.getLogger().info("Test1 onEnable...");
            Configuration.load(TestConfig.class, this);
            this.initializeDatabase();
            this.testDatabase();
        }
        catch (SQLException ex)
        {
            Logger.getLogger(CubeTest.class.getName()).log(Level.SEVERE, null, ex);
        }

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

    public void testDatabase() throws SQLException
    {

        Database database = this.getDatabase();
        Object[] obj = new Object[3];
        obj[0] = new Date(2012, 8, 8);
        obj[1] = 10;
        obj[2] = "Heinz";
        database.preparedExecute(TestModel.class, "store", obj);
        obj[0] = new Date(2012, 6, 8);
        obj[1] = 30;
        obj[2] = "Peter";
        database.preparedExecute(TestModel.class, "store", obj);
        obj[0] = new Date(2012, 8, 6);
        obj[1] = 20;
        obj[2] = "Manfred";
        database.preparedExecute(TestModel.class, "store", obj);

        obj[0] = new Date(2012, 8, 8);
        obj[1] = 20;
        obj[2] = "Heinz";
        database.preparedExecute(TestModel.class, "store", obj);
        obj[0] = new Date(2012, 6, 8);
        obj[1] = 120;
        obj[2] = "Peter";
        database.preparedExecute(TestModel.class, "store", obj);
        obj[0] = new Date(2012, 8, 6);
        obj[1] = 50;
        obj[2] = "Manfred";
        database.preparedExecute(TestModel.class, "store", obj);
        
        database.preparedQuery(TestModel.class, "get", 2);
        database.preparedQuery(TestModel.class, "getall");
        Object[] obj2 = new Object[4];
        obj2[0] = obj[0];
        obj2[1] = 100;
        obj2[2] = obj[2];
        obj2[3] = 3;
        database.preparedExecute(TestModel.class, "update", obj2);
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
