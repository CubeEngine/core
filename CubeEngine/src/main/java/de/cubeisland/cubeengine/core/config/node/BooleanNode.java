package de.cubeisland.cubeengine.core.config.node;

public class BooleanNode extends Node {
    private boolean bool;

    public BooleanNode(Boolean bool) {
        this.bool = bool;
    }

    public boolean getValue() {

        return this.bool;
    }

    @Override
    public String unwrap() {
        return String.valueOf(bool);
    }
}
