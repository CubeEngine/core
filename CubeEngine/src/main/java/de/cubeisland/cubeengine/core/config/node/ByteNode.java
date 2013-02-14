package de.cubeisland.cubeengine.core.config.node;

public class ByteNode extends Node
{
    private byte value;

    public ByteNode(Byte value)
    {
        this.value = value;
    }

    public byte getValue()
    {

        return this.value;
    }

    @Override
    public String unwrap()
    {
        return String.valueOf(value);
    }
}
