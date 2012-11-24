package de.cubeisland.cubeengine.rules;

import de.cubeisland.cubeengine.core.filesystem.Resource;

public enum RuleBookResource implements Resource
{
    GERMAN_MESSAGES(
        "resources/language/messages/de_DE.json",
        "language/de_DE/rulebook.json"),
    FRENCH_MESSAGES(
        "resources/language/messages/fr_FR.json",
        "language/fr_FR/rulebook.json");
    private final String target;
    private final String source;

    private RuleBookResource(String source, String target)
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
