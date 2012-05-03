package de.cubeisland.CubeWar;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class Heroes {

    private static Map<Player,Hero> heroes= new HashMap<Player,Hero>();
    
    public Heroes() 
    {
    
    }
    
    public static void kill(Player killer,Player killed)
    {
        getHero(killer).kill(getHero(killed));
        getHero(killed).death();
    }
    
    private static Hero getHero(Player hero)
    {
        if (heroes.containsKey(hero))
            return heroes.get(hero);
        else
        {
            heroes.put(hero, new Hero(hero));
            return heroes.get(hero);        
        }
    }
}
