package de.cubeisland.cubeengine.core.config.node;

public class LongNode extends Node {
    private long value;

    public LongNode(Long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String unwrap() {
        return String.valueOf(value);
    }
}
