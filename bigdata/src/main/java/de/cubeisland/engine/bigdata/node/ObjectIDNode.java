package de.cubeisland.engine.bigdata.node;

import de.cubeisland.engine.reflect.node.Node;
import org.bson.types.ObjectId;

public class ObjectIdNode extends Node<ObjectId>
{
    private ObjectId value;

    public ObjectIdNode(ObjectId value)
    {
        this.value = value;
    }

    public static Node of(ObjectId value)
    {
        return new ObjectIdNode(value);
    }

    @Override
    public String asText()
    {
        return value.toString();
    }

    @Override
    public ObjectId getValue()
    {
        return this.value;
    }

    @Override
    public String toString()
    {
        return this.value.toString();
    }
}
