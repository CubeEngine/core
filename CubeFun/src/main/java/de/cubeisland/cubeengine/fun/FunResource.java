package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.filesystem.Resource;

public enum FunResource implements Resource
{
    GERMAN_MESSAGES(
        "resources/language/messages/de_DE.json",
        "language/de_DE/fun.json");
    private final String target;
    private final String source;

    private FunResource(String source, String target)
    {
        this.source = source;
        this.target = target;
    }

    @Override
    public String getSource()
    {
        return this.source;
    }

    @Override
    public String getTarget()
    {
        return this.target;
    }
}
