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
    command_claim_ownTeam("claim_deny_ownTeam"),
    command_claim_radius("claim_deny_radius"),
    command_claim_otherTeam("claim_deny_otherTeam"),
    command_claim_fromother("claim_deny_other"),
    command_claim_peaceful("claim_deny_other_never"),
    command_claim_bypass(null),
    
    command_unclaim_ownTeam("unclaim_deny_ownTeam"),
    command_unclaim_radius("unclaim_deny_radius"),
    command_unclaim_ownTeam_all("unclaim_deny_ownTeam_all"),
    command_unclaim_otherTeam("unclaim_deny_otherTeam"),
    command_unclaim_otherTeam_all("unclaim_deny_otherTeam_all"),
    command_unclaim_allTeam("unclaim_deny_allTeam"),
    command_unclaim_allTeam_all("unclaim_deny_allTeam_all"),
    command_unclaim_bypass(null),
    
    command_relation_change("relation_change"),
    command_relation_change_other("relation_change_other"),
    
    command_invite("invite_deny"),
    
    command_kick("kick_deny"),
    command_kick_other("kick_deny_other"),
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
