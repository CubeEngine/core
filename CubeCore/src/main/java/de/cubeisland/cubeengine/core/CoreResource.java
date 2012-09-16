package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.filesystem.Resource;

/**
 * Holds all the resource of the core
 *
 * @author Phillip Schichtel
 */
public enum CoreResource implements Resource
{
    GERMAN_META("resources/language/de_DE.yml", "language/de_DE.yml"),
    FRENCH_META("resources/language/fr_FR.yml", "language/fr_FR.yml"),
    GERMAN_MESSAGES("resources/language/messages/de_DE.json", "language/de_DE/core.json"),
    ENCHANTMENTS("resources/enchantments.txt", "data/enchantments.txt"),
    ITEMS("resources/items.txt", "data/items.txt"),
    DATAVALUES("resources/datavalues.txt", "data/datavalues.txt"),
    ENTITIES("resources/entities.txt", "data/entities.txt");
    private final String target;
    private final String source;

    private CoreResource(String source, String target)
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