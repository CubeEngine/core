package de.cubeisland.CubeWar;

/**
 *
 * @author Faithcaio
 */
public class Rank {

    private final CubeWarConfiguration config = CubeWar.getInstance().getConfiguration();
    
    private String name;
    private int killmodifier;
    private int deathmodifier;
    private int killpointlimit;
    
    public Rank(String name, int killMod, int deathMod, int kplimit) 
    {
        this.name = name;
        this.deathmodifier = deathMod;
        this.killmodifier = killMod;
        this.killpointlimit = kplimit;
    }
    
    public Rank newRank(Hero hero)
    {
        Integer kp = null;
        for (int i = hero.getKp();kp==null;--i)
        {
            if (config.cubewar_ranks.containsKey(i))
                kp = i;
        }
        return config.cubewar_ranks.get(kp);
    }
    
    public int getKmod()
    {
        return this.killmodifier;
    }
    
    public int getDmod()
    {
        return this.deathmodifier;
    }
    
    public String getName()
    {
        return this.name;
    }
}
