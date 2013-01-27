package de.cubeisland.cubeengine.core.config.node;

public class StringNode extends Node {

    private String value;


    public StringNode(String string) {
        this.value = string;

    }

    public String getValue() {
        return value;
    }

    public void setValue(String string) {
        this.value = string;
    }

    @Override
    public String unwrap() {
        return value;
    }

    public static StringNode of(String string)
    {
        return new StringNode(string);
    }
}
