package de.cubeisland.cubeengine.core.i18n;

import java.util.Locale;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;

/**
 * This cofniguration is used to parse the language configurations.
 */
@Codec("yaml")
public class LocaleConfig extends Configuration
{
    @Option("code")
    public Locale locale;
    @Option("name")
    public String name;
    @Option("localname")
    public String localName;
    @Option("parent")
    public Locale parent = null;
    @Option("clones")
    public Locale[] clones = null;
}
