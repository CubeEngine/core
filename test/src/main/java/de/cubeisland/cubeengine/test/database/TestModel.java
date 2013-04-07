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
package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import java.sql.Date;

@SingleKeyEntity(tableName = "orders", primaryKey = "id", autoIncrement = true)
public class TestModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long id;
    @Attribute(type = AttrType.DATE)
    public Date orderDate;
    @Attribute(type = AttrType.DOUBLE)
    public double orderPrice;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public String customer;

    public TestModel()
    {}

    public TestModel(Date orderDate, double orderPrice, String customer)
    {
        this.id = -1;
        this.orderDate = orderDate;
        this.orderPrice = orderPrice;
        this.customer = customer;
    }

    @Override
    public Long getKey()
    {
        return this.id;
    }

    @Override
    public void setKey(Long key)
    {
        this.id = key;
    }
}
