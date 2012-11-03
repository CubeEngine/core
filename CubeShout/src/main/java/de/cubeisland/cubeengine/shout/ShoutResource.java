package de.cubeisland.cubeengine.shout;

import de.cubeisland.cubeengine.core.filesystem.Resource;

public enum ShoutResource implements Resource
{
	
	NORWEGIAN_MESSAGES("resources/language/messages/nb_NO.json", "language/nb_NO/writer.json"),
	EXAMPLE_ANNOUNCEMENT_CONFIG("resources/ExampleAnnouncement/announcement.yml", "modules/Shout/ExapleAnnouncement/announcement.yml"),
	EXAMPLE_ANNOUNCEMENT_en_US("resources/ExampleAnnouncement/en_US.txt", "modules/Shout/ExapleAnnouncement/en_US.txt");
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
