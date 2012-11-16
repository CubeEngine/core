package de.cubeisland.cubeengine.core.i18n;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;

/**
 * This cofniguration is used to parse the language configurations.
 */
@Codec("yaml")
public class LanguageConfiguration extends Configuration
{
    @Option("code")
    public String code;
    @Option("name")
    public String name;
    @Option("localname")
    public String localName;
    @Option("parent")
    public String parent = null;
    @Option(value = "clones")
    public String[] clones = null;
}
