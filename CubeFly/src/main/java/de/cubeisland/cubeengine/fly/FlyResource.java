/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.filesystem.Resource;

/**
 *
 * @author Anselm Brehme
 */
public enum FlyResource implements Resource
{

    GERMAN_MESSAGES("resources/language/messages/de_DE.json", "language/de_DE/fly.json");
    private final String target;
    private final String source;

    private FlyResource(String source, String target)
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
