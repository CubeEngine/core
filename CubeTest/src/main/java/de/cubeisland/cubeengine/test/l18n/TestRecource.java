package de.cubeisland.cubeengine.test.l18n;

import de.cubeisland.cubeengine.core.filesystem.Resource;

/**
 *
 * @author Anselm Brehme
 */
public enum TestRecource implements Resource
{
    GERMAN_MESSAGES("resources/language/messages/de_DE.json", "language/de_DE/test.json"),
    FRENCH_MESSAGES("resources/language/messages/fr_FR.json", "language/fr_FR/test.json");
    private final String target;
    private final String source;

    private TestRecource(String source, String target)
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
