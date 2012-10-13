package de.cubeisland.cubeengine.writer;

import de.cubeisland.cubeengine.core.filesystem.Resource;

public enum WriterResource implements Resource
{
    
    NORWEGIAN_MESSAGES("resources/language/messages/nb_NO.json", "language/nb_NO/writer.json");
    private final String target;
    private final String source;

    private WriterResource(String source, String target)
    {
        this.source = source;
        this.target = target;
    }

    public String getSource()
    {
        return this.source;
    }

    public String getTarget()
    {
        return this.target;
    }
}