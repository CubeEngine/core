package de.cubeisland.cubeengine.core.config.node;

public class FloatNode extends Node
{
    private float value;

    public FloatNode(Float value)
    {
        this.value = value;
    }

    public float getValue()
    {
        return value;
    }

    @Override
    public String unwrap()
    {
        return String.valueOf(value);
    }
}
