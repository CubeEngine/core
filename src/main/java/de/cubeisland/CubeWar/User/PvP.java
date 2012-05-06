package de.cubeisland.CubeWar.User;

import de.cubeisland.CubeWar.CubeWar;
import static de.cubeisland.CubeWar.CubeWar.t;
import de.cubeisland.CubeWar.Groups.Group;
import de.cubeisland.CubeWar.Groups.GroupControl;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class PvP{

    public PvP() 
    {
    
    }
    
    public static boolean isFriendlyFireOn(Player damager, Player damagee)
    {
        CubeWar.debug("FF-ON?");
        //if (!PvP.isDamageOn(damager, damagee)) return false;
        //CubeWar.debug("Damage-ON");
        if (PvP.isAlly(damager, damagee))
        {
            CubeWar.debug("isAlly");
            if (PvP.isAreaDenyingFF(damager, damagee)) return false;
            CubeWar.debug("Area-FF");
            CubeWar.debug("FF-ON!");
            return true;
        }
        else
        {
            CubeWar.debug("isNoAlly");
            return true;
        }
    }
    
    public static boolean isDamageOn(Player damager, Player damagee)
    {
        CubeWar.debug("Damage-ON?");
        //if (!PvP.isPvPallowed(damager, damagee)) return false;
        //CubeWar.debug("PVP-ON!");
        if (PvP.isAreaDenyingDamage(damager, damagee)) return false;
        CubeWar.debug("Area-ON");
        if (PvP.isPlayerRespawning(damager, damagee)) return false;
        CubeWar.debug("User-not-respawning");
        CubeWar.debug("Damage-ON!");
        return true;
    }
    
    public static boolean isPvPallowed(Player damager, Player damagee)
    {
        CubeWar.debug("PVP-ON?");
        if (PvP.isAreaPvPOff(damager, damagee)) return false;
        CubeWar.debug("Area-ON");
        if (PvP.isHeroPeaceFull(damager)||PvP.isHeroPeaceFull(damagee)) return false;
        CubeWar.debug("User-ON");
        CubeWar.debug("PVP-ON!");
        return true;
    }
    
    private static boolean isHeroPeaceFull(Player player)
    {
        return Users.getUser(player).getMode().equals(PlayerMode.PEACE);
    }
    
    private static boolean isAreaPvPOff(Player damager, Player damagee)
    {
        if (GroupControl.getArea(damager).getBits().isset(Group.PVP_ON)) return false;
        if (GroupControl.getArea(damagee).getBits().isset(Group.PVP_ON)) return false;
        return true;
    }
    
    private static boolean isPlayerRespawning(Player damager, Player damagee)
    {
        if (Users.getUser(damager).isRespawning()) return true;
        if (Users.getUser(damagee).isRespawning()) return true;
        return false;
    }
    
    private static boolean isAreaDenyingFF(Player damager, Player damagee)
    {
        if (GroupControl.getArea(damager).getBits().isset(Group.PVP_FRIENDLYFIRE)) return false;
        if (GroupControl.getArea(damagee).getBits().isset(Group.PVP_FRIENDLYFIRE)) return false;
        return true;
    }
    
    private static boolean isAreaDenyingDamage(Player damager, Player damagee)
    {
        if (GroupControl.getArea(damager).getBits().isset(Group.PVP_DAMAGE)) return false;
        if (GroupControl.getArea(damagee).getBits().isset(Group.PVP_DAMAGE)) return false;
        return true;
    }
    
    public static int modifyDamage(Player damager, Player damagee, int damage)
    {
        int dmg = damage;
        Map<Group.DmgModType,Integer> modifiers = GroupControl.getArea(damagee).getDamagemodifier();
        Integer tmp;
        for (Group.DmgModType type : Group.DmgModType.values())
        {
            tmp = modifiers.get(type);
            if (tmp != null)
            {
                switch (type)
                {
                    case ADD:
                    {
                        dmg += tmp;
                    }
                    case PERCENT:
                    {
                        dmg *= 1 + (tmp / 100);
                    } 
                    case SET:
                    {
                        dmg = tmp;
                    }
                }
                break;
            }
        }
        return dmg;
    }
    
    private static boolean isAlly(Player damager, Player damagee)
    {
        return Users.isAllied(damager, damagee);
    }
    
    
    public static void stopFlyArrow(Player player)
    {
        
        player.sendMessage(t("event_arrow"));
        PvP.stopFlyAndFall(player);
    }
    
    public static void stopFlyAndFall(Player player)
    {
        CubeWar.debug("Fly Stop + Fall");
        player.setFlying(false);
        player.setAllowFlight(false);
    }
    
    public static void stopFly(Player player)
    {
        CubeWar.debug("Fall");
        player.setFlying(false);        
    }
}
