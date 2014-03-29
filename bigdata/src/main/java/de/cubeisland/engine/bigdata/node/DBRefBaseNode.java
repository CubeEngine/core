package de.cubeisland.engine.bigdata.node;

import com.mongodb.DBRefBase;
import de.cubeisland.engine.reflect.node.Node;

public class DBRefBaseNode extends Node<DBRefBase>
{
    private DBRefBase value;

    public DBRefBaseNode(DBRefBase value)
    {
        this.value = value;
    }

    public static Node of(DBRefBase value)
    {
        return new DBRefBaseNode(value);
    }

    @Override
    public String asText()
    {
        return value.toString();
    }

    @Override
    public DBRefBase getValue()
    {
        return this.value;
    }

    @Override
    public String toString()
    {
        return this.value.toString();
    }
}
