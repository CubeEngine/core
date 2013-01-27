package de.cubeisland.cubeengine.core.config.node;

public class ShortNode  extends Node {
    private short value;

    public ShortNode(Short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
    @Override
    public String unwrap() {
        return String.valueOf(value);
    }
}
