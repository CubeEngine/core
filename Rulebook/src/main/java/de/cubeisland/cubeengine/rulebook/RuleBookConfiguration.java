package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import java.util.HashSet;
import java.util.Set;

public class RuleBookConfiguration extends Configuration
{    
    @Comment("Registered the available languages of the Rulebooks.")
    @Option("languages")
    public Set<String> languages = new HashSet<String>()
    {
        {
            add(CubeEngine.getI18n().getDefaultLanguage());
        }
    };
}
