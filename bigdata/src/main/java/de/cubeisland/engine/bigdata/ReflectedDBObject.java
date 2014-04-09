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
package de.cubeisland.engine.bigdata;

import com.mongodb.DBObject;
import de.cubeisland.engine.reflect.Reflected;

public class ReflectedDBObject extends Reflected<MongoDBCodec, DBObject>
{
    @Override
    public void save(DBObject dbo)
    {
        this.getCodec().saveReflected(this, dbo);
    }

    @Override
    public boolean loadFrom(DBObject dbo)
    {
        this.getCodec().loadReflected(this, dbo);
        return true;
    }
}
