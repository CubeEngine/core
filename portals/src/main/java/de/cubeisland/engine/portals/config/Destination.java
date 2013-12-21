package de.cubeisland.engine.portals.config;

public class Destination
{
    private Type type;


    public enum Type
    {
        PORTAL, WORLD, LOCATION;
    }
    // TODO portal | loc /w world | worldspawn
}
