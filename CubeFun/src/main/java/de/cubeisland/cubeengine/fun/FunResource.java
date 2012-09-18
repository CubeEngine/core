package de.cubeisland.cubeengine.cubefun;

import de.cubeisland.cubeengine.core.filesystem.Resource;

/**
 *
 * @author Wolfi
 */
public enum FunResource implements Resource
{
    GERMAN_MESSAGES("resources/language/messages/de_DE.json", "language/de_DE/fun.json");
    private final String target;
    private final String source;

    private FunResource(String source, String target)
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
