package de.cubeisland.cubeengine.core.config.node;

public class NullNode extends Node
{
    private NullNode()
    {}

    public static NullNode emptyNode()
    {
        return new NullNode();
    }

    @Override
    public String unwrap()
    {
        return "";
    }
}
