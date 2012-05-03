package de.cubeisland.CubeWar;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
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
    
    public static void kill(Player killerPlayer,Player killedPlayer)
    {
        Hero killer = getHero(killerPlayer);
        Hero killed = getHero(killedPlayer);
        if (killer.equals(killed))
        {
            //Suicide ?
            killerPlayer.sendMessage("SELFOWNED!");
        }
        killerPlayer.sendMessage("You killed "+getHeroKD(killed));
        killedPlayer.sendMessage("You got killed by "+getHeroKD(killer));
        killer.kill(killed);
        killed.die();
    }
    
    public static String getHeroKD(Hero hero)
    {
        String out = hero.getName()+"("+hero.getKills()+":"+hero.getDeath()+")";
        return out;
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
