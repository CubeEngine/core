package de.cubeisland.cubeengine.shout;

import de.cubeisland.cubeengine.core.filesystem.Resource;

public enum ShoutResource implements Resource
{
	
	NORWEGIAN_MESSAGES("resources/language/messages/nb_NO.json", "language/nb_NO/writer.json");
    private final String target;
    private final String source;

    private ShoutResource(String source, String target)
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
