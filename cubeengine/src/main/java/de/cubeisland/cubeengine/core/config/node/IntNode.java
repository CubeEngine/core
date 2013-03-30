package de.cubeisland.cubeengine.core.config.node;

public class IntNode extends Node
{

    private int value;

    public IntNode(Integer value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    @Override
    public String unwrap()
    {
        return String.valueOf(value);
    }
}
