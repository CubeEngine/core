package de.cubeisland.cubeengine.core.config.node;

public class DoubleNode extends Node
{
    private double value;

    public DoubleNode(Double value)
    {
        this.value = value;
    }

    public double getValue()
    {
        return value;
    }

    @Override
    public String unwrap()
    {
        return String.valueOf(value);
    }
}
