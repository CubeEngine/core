package de.cubeisland.cubeengine.core.persistence.filesystem;

/**
 *
 * @author Faithcaio
 */
public class CoreConfiguration extends Configuration
{
    @Option("debug")
    public boolean debugMode = false;

    @Option("defaultLanguage")
    public String defaultLanguage = "en_US";
}
