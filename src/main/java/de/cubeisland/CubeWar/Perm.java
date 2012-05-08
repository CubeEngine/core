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
    command_claim_ownTeam(""),//TODO msg
    command_claim_radius(""),//TODO msg
    command_claim_otherTeam(""),//TODO msg
    command_claim_fromother("claim_deny_other"),
    command_claim_peaceful("claim_deny_other_never"),
    command_claim_bypass(null),
    
    command_unclaim_ownTeam(""),//TODO msg
    command_unclaim_radius(""),//TODO msg
    command_unclaim_ownTeam_all(""),//TODO msg
    command_unclaim_otherTeam(""),//TODO msg
    command_unclaim_otherTeam_all(""),//TODO msg
    command_unclaim_allTeam(""),//TODO msg
    command_unclaim_allTeam_all(""),//TODO msg
    command_unclaim_bypass(null),
    
    command_relation_change(""),//TODO msg
    command_relation_change_other(""),//TODO msg
    
    command_invite(""),//TODO msg
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
