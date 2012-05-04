package de.cubeisland.CubeWar;

import static de.cubeisland.CubeWar.CubeWar.t;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Effect;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Monster;
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
            if (killer.getMode().equals(PlayerMode.HIGHLANDER))
                killerPlayer.sendMessage(t("killer",getHeroKD(killed, killed, killed.getKills()+1)));
            else
                killerPlayer.sendMessage(t("killer",getHeroKD(killed, killed, 1)));
            killedPlayer.sendMessage(t("killed",getHeroKD(killer, killed, -1)));
            killer.kill(killed);
            killed.die();
        }
    }
    
    public static void kill(Player killerPlayer, Monster monster)
    {
        Hero killer = getHero(killerPlayer);
        killer.kill(monster);           
    }
    
    public static String getHeroKD(Hero hero, Hero killed, int kill)
    {
        String name = hero.getName();
        int k = hero.getKills();
        int d = hero.getDeath();  
        if (kill==0)
        {
            return t("kd",hero.getRank().getName(),name,k,d,hero.getKp());
        }
        switch (hero.getMode())
        {
            case NORMAL:
            {
                if (kill > 0) return t("kds-",hero.getRank().getName(),name,t("kd_n-",k,d),hero.getKp(),hero.getRank().getDmod());
                else return t("kds+",hero.getRank().getName(),name,t("kd_n+",k,d),hero.getKp(),killed.getRank().getKmod());
            }
            case KILLRESET:
            {
                if (kill > 0) return t("kds-",hero.getRank().getName(),name,t("kd_kr-",k,d),hero.getKp(),hero.getRank().getDmod());
                else return t("kds+",hero.getRank().getName(),name,t("kd_kr+",k,d),hero.getKp(),killed.getRank().getKmod());
            }
            case HIGHLANDER:  
            {
                if (kill > 0) return t("kds-",hero.getRank().getName(),name,t("kd_h-",k,d),hero.getKp(),hero.getRank().getKmod());
                else return t("kds+",hero.getRank().getName(),name,t("kd_h+",k,d),hero.getKp(),killed.getRank().getKmod());
            }
        }
        return "#ERROR while getting KD";
    }
    
    public static Hero getHero(Player hero)
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
