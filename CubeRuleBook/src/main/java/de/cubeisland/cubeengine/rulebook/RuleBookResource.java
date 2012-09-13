/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.filesystem.Resource;

/**
 *
 * @author Wolfi
 */
public enum RuleBookResource implements Resource
{
    GERMAN_MESSAGES("resources/language/messages/de_DE.json", "language/de_DE/rulebook.json"),
    FRENCH_MESSAGES("resources/language/messages/fr_FR.json", "language/fr_FR/rulebook.json");
    
    private final String target;
    private final String source;

    private RuleBookResource(String source, String target)
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
