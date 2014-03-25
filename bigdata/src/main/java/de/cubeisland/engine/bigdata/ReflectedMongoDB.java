package de.cubeisland.engine.bigdata;

import com.mongodb.DBObject;
import de.cubeisland.engine.reflect.Reflected;

public class ReflectedMongoDB extends Reflected<MongoDBCodec, DBObject>
{
    @Override
    public void save(DBObject dbObject)
    {
        this.getCodec().saveReflected(this, dbObject);
    }

    @Override
    public boolean loadFrom(DBObject dbObject)
    {
        this.getCodec().loadReflected(this, dbObject);
        return true;
    }
}
