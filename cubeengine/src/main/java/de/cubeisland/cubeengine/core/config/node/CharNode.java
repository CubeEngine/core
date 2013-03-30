package de.cubeisland.cubeengine.core.config.node;

public class CharNode extends Node
{
    private char value;

    public CharNode(Character value)
    {
        this.value = value;
    }

    public char getValue()
    {
        return value;
    }

    @Override
    public String unwrap()
    {
        return String.valueOf(value);
    }
}
