package de.cubeisland.CubeWar;

import static de.cubeisland.CubeWar.CubeWar.t;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

/**
 *
 * @author Faithcaio
 */
public enum Perm
{
    command_claim("Perm_claim"),
    command_claim_ownTeam("Perm_claim_ownTeam"),
    command_claim_radius("Perm_claim_radius"),
    command_claim_otherTeam("Perm_claim_otherTeam"),
    command_claim_fromother("Perm_claim_other"),
    command_claim_peaceful("Perm_claim_other_never"),
    command_claim_bypass(null),
    
    command_unclaim("Perm_unclaim"),
    command_unclaim_ownTeam("Perm_unclaim_ownTeam"),
    command_unclaim_radius("Perm_unclaim_radius"),
    command_unclaim_ownTeam_all("Perm_unclaim_ownTeam_all"),
    command_unclaim_otherTeam("Perm_unclaim_otherTeam"),
    command_unclaim_otherTeam_all("Perm_unclaim_otherTeam_all"),
    command_unclaim_allTeam("Perm_unclaim_allTeam"),
    command_unclaim_allTeam_all("Perm_unclaim_allTeam_all"),
    command_unclaim_bypass(null),
    
    command_relation_change("relation_change"),//For neutral- ally- enemy- Commands
    command_relation_change_other("relation_change_other"),
    
    command_invite("Perm_invite"),
    command_uninvite("Perm_uninvite"),
    command_kick("Perm_kick"),
    command_kick_other("Perm_kick_other"),
    command_leave("Perm_leave"),
    command_join("Perm_join"),
    command_position("Perm_teampos"),
    command_position_admin("Perm_teampos_mod"),
    command_position_mod("Perm_teampos_admin"),
    
    command_info("Perm_info"),
    command_info_other("Perm_info_other"),
    command_whois("Perm_whois"),
    command_whois_other("Perm_whois_other"),
    
    command_modify("Perm_modify"),
    command_create("Perm_create"),
    command_create_team("Perm_create_team"),
    command_create_arena("Perm_create_arena"),
    
    command_fly("Perm_fly"),
    command_bounty("Perm_bounty"),
    
    ;
    private final String text;
    private final String permission;

    private Perm(final String text)
    {
        this.text = text;
        this.permission = "cubewar." + this.name().toLowerCase().replace("_", ".");
    }

    private boolean checkPerm(Permissible sender)
    {
        return sender.hasPermission(permission);
    }

    public boolean hasPerm(CommandSender sender)
    {
        if (this.checkPerm(sender))
        {
            return true;
        }
        else
        {
            this.send(sender);
            return false;
        }
    }

    public boolean hasNotPerm(CommandSender sender)
    {
        return !this.hasPerm(sender);
    }

    private void send(CommandSender sender)
    {
        if (this.text != null)
        {
            sender.sendMessage(t("perm") + t(this.text));
        }
    }
}
