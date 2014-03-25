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

import java.util.Collection;
import java.util.Collections;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import de.cubeisland.engine.reflect.Reflected;
import de.cubeisland.engine.reflect.codec.Codec;
import de.cubeisland.engine.reflect.exception.CodecIOException;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.ErrorNode;
import de.cubeisland.engine.reflect.node.MapNode;
import de.cubeisland.engine.reflect.node.Node;

public class MongoDBCodec extends Codec<DBObject, DBCollection>
{
    @Override
    public Collection<ErrorNode> loadReflected(Reflected reflected, DBObject dbo)
    {
        try
        {
            return dumpIntoSection(reflected.getDefault(), reflected, this.load(dbo, reflected), reflected);
        }
        catch (ConversionException ex)
        {
            if (reflected.useStrictExceptionPolicy())
            {
                throw new CodecIOException("Could not load reflected", ex);
            }
            reflected.getLogger().warning("Could not load reflected" + ex);
            return Collections.emptyList();
        }
    }

    @Override
    public void saveReflected(Reflected reflected, DBCollection dbc)
    {
        try
        {
            this.save(convertSection(reflected.getDefault(), reflected, reflected), dbc, reflected);
        }
        catch (ConversionException ex)
        {
            if (reflected.useStrictExceptionPolicy())
            {
                throw new CodecIOException("Could not save reflected", ex);
            }
            reflected.getLogger().warning("Could not save reflected" + ex);
        }
    }

    @Override
    protected void save(MapNode mapNode, DBCollection dbc, Reflected reflected) throws ConversionException
    {
        // TODO create DBObject & insert to dbc
    }

    @Override
    protected MapNode load(DBObject dbo, Reflected reflected) throws ConversionException
    {
        return (MapNode) Node.wrapIntoNode(dbo.toMap()); // TODO test if this works
    }
}
