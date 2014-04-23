/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.test.tests;

import java.sql.SQLException;

public class DatabaseTest extends Test
{
    private final de.cubeisland.engine.test.Test module;

    public DatabaseTest(de.cubeisland.engine.test.Test module)
    {
        this.module = module;
    }

    @Override
    public void onEnable() throws Exception
    {
        this.initializeDatabase();
        this.testDatabase();
        this.setSuccess(true);
    }

    public void initializeDatabase() throws SQLException
    {//TODO DATABASE
        /*
        Database db = this.getCore().getDB();
        try
        {
            db.execute(db.getQueryBuilder().dropTable("Orders").end());
        }
        catch (Exception ignore)
        {}
        this.manager = new TestManager(db);*/
    }

    public void testDatabase() throws SQLException
    {
        //TODO DATABASE
        /*
        Database database = this.getCore().getDB();

        try
        {//Clears the TestLogs in Database (This does always fail with new db)
            database.execute(database.getQueryBuilder().truncateTable("test_log").end());
        }
        catch (Exception ignored)
        {}

        this.manager.store(new TestModel(this.getDate(2012, 8, 8), 10, "Heinz"), false);
        this.manager.store(new TestModel(this.getDate(2012, 6, 8), 30, "Hans"), false);
        this.manager.store(new TestModel(this.getDate(2012, 8, 6), 20, "Manfred"), false);
        this.manager.store(new TestModel(this.getDate(2012, 8, 8), 20, "Heinz"), false);
        this.manager.store(new TestModel(this.getDate(2012, 8, 8), 120, "Hans"), false);
        this.manager.store(new TestModel(this.getDate(2011, 2, 8), 50, "Manfred"), false);
        this.manager.get(2L);
        this.manager.getAll();
        TestModel model = this.manager.get(3L);
        model.orderDate = this.getDate(111, 2, 2);
        model.orderPrice = 100;
        model.customer = "Paul";
        this.manager.update(model);
        */
    }
}
