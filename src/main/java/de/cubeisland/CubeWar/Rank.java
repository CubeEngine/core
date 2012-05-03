package de.cubeisland.CubeWar;

/**
 *
 * @author Faithcaio
 */
public class Rank {

    private String name;
    private int killmodifier;
    private int deathmodifier;
    
    public Rank(String name, int killMod, int deathMod) 
    {
        this.name = name;
        this.deathmodifier = deathMod;
        this.killmodifier = killMod;
    }
}
