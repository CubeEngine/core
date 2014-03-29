package de.cubeisland.engine.bigdata;

import com.mongodb.DBRefBase;
import de.cubeisland.engine.reflect.Reflector;

public class Reference<T extends ReflectedMongoDB>
{
    public Reference(Reflector reflector, DBRefBase dbRefBase)
    {
        this.reflector = reflector;
        this.dbRef = dbRefBase;
    }

    private final Reflector reflector;
    protected DBRefBase dbRef;
    private T fetched = null;

    public T fetch(Class<T> clazz)
    {
        if (fetched == null)
        {
            this.fetched = this.reflector.load(clazz, new RDBObject(dbRef.fetch()));
        }
        return fetched;
    }
}
