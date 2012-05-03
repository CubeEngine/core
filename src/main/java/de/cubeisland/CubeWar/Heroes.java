package de.cubeisland.CubeWar;

import static de.cubeisland.CubeWar.CubeWar.t;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Effect;
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
            killerPlayer.sendMessage(t("selfown"));
            killed.die();
            killerPlayer.playEffect(killerPlayer.getLocation(), Effect.POTION_BREAK, 0);
        }
        else
        {
            if (killer.getMode().equals(Mode.HIGHLANDER))
                killerPlayer.sendMessage(t("killer",getHeroKD(killed, killed.getKills()+1)));
            else
                killerPlayer.sendMessage(t("killer",getHeroKD(killed, 1)));
            killedPlayer.sendMessage(t("killed",getHeroKD(killer,-1)));
            killer.kill(killed);
            killed.die();
        }
    }
    
    public static String getHeroKD(Hero hero, int kill)
    {
        String name = hero.getName();
        int k = hero.getKills();
        int d = hero.getDeath();  
        if (kill==0)
        {
            return t("kd",name,k,d);
        }
        switch (hero.getMode())
        {
            case NORMAL:
            {
                if (kill > 0) return t("kd_n+",name,k,d);
                else return t("kd_n-",name,k,d);
            }
            case KILLRESET:
            {
                if (kill > 0) return t("kd_kr+",name,k,d);
                else return t("kd_kr-",name,k,d);
            }
            case HIGHLANDER:  
            {
                if (kill > 0) return t("kd_h+",name,k,kill,d);
                else return t("kd_h-",name,k,d);
            }
                
        }
        return "#ERROR while getting KD";
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
