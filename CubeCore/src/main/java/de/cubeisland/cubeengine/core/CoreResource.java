package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.persistence.filesystem.Resource;

/**
 * Holds all the resource of the core
 *
 * @author Phillip Schichtel
 */
public enum CoreResource implements Resource
{
    GEOIP_DATABASE("GeoIP.dat", "GeoIP.dat"),
    ENGLISH_META("language/en_US.json", "language/en_US.json"),
    GERMAN_META("language/de_DE.json", "language/de_DE.json"),
    GERMAN_MESSAGES("language/messages/de_DE.json", "language/de_DE/core.json");

    private final String target;
    private final String source;
    
    private CoreResource(String source, String target)
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
