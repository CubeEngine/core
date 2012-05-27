package de.cubeisland.cubeengine.core.persistence.filesystem;

/**
 * Holds all the resource of the core
 *
 * @author Phillip Schichtel
 */
public enum Resource
{
    GEOIP_DATABASE("GeoIP.dat"),
    ENGLISH_META("languages/english.json"),
    GERMAN_META("languages/german.json"),
    GERMAN_MESSAGES("languages/messages/german/core.json");

    private final String path;
    private Resource(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return this.path;
    }
}
