package de.cubeisland.cubeengine.core.i18n;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;

/**
 *
 * @author Phillip Schichtel
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
}
