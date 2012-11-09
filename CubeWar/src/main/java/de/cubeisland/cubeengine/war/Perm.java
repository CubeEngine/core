package de.cubeisland.cubeengine.war;

import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.groups.Group;
import de.cubeisland.cubeengine.war.user.WarUser;
import de.cubeisland.cubeengine.war.user.UserControl;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

/**
 *
 * @author Anselm Brehme
 */
public enum Perm
{
    //TODO Permissions dynamisch registrieren
    command_claim("Perm_claim"),
    command_claim_ownTeam("Perm_claim_ownTeam"),
    command_claim_radius("Perm_claim_radius"),
    command_claim_otherTeam("Perm_claim_otherTeam"),
    command_claim_fromother("Perm_claim_other"),
    command_claim_BP(null),
    command_unclaim("Perm_unclaim"),
    command_unclaim_ownTeam("Perm_unclaim_ownTeam"),
    command_unclaim_radius("Perm_unclaim_radius"),
    command_unclaim_ownTeam_all("Perm_unclaim_ownTeam_all"),
    command_unclaim_otherTeam("Perm_unclaim_otherTeam"),
    command_unclaim_otherTeam_all("Perm_unclaim_otherTeam_all"),
    command_unclaim_allTeam("Perm_unclaim_allTeam"),
    command_unclaim_allTeam_all("Perm_unclaim_allTeam_all"),
    command_unclaim_BP(null),
    command_relation_change("relation_change"),//For neutral- ally- enemy- Commands
    command_relation_change_other("relation_change_other"),
    command_relation_BP(null),
    command_invite("Perm_invite"),
    command_uninvite("Perm_uninvite"),
    command_kick("Perm_kick"),
    command_kick_other("Perm_kick_other"),
    command_leave("Perm_leave"),
    command_join("Perm_join"),
    command_membercontrol_BP(null),
    command_position("Perm_teampos"),
    command_position_admin("Perm_teampos_mod"),
    command_position_mod("Perm_teampos_admin"),
    command_position_BP(null),
    command_info("Perm_info"),
    command_info_other("Perm_info_other"),
    command_whois("Perm_whois"),
    command_whois_other("Perm_whois_other"),
    command_modify("Perm_modify"),
    command_modify_BP(null),
    command_description("Perm_desc"),//TODO
    command_description_other("Perm_desc_other"),//TODO
    command_peacefull("Perm_peace"),//TODO
    command_create("Perm_create"),
    command_create_team("Perm_create_team"),
    command_create_arena("Perm_create_arena"),
    command_create_BP(null),
    command_protection_BP(null),
    command_bypass("Perm_Perm"),
    command_bypass_claim("Perm_Perm_mode"),
    command_bypass_unclaim("Perm_Perm_mode"),
    command_bypass_relation("Perm_Perm_mode"),
    command_bypass_membercontrol("Perm_Perm_mode"),
    command_bypass_position("Perm_Perm_mode"),
    command_bypass_create("Perm_Perm_mode"),
    command_bypass_modify("Perm_Perm_mode"),
    command_bypass_protection("Perm_Perm_mode"),
    command_bypass_fly("Perm_Perm_mode"),;
    private final String text;
    private final String permission;
    private final CubeWarConfiguration config = CubeWar.getInstance().getConfiguration();
    private UserControl users = UserControl.get();

    private Perm(final String text)
    {
        this.text = text;
        this.permission = "cubewar." + this.name().toLowerCase().replace("_", ".");
    }

    public boolean checkPerm(Permissible sender)
    {
        return sender.hasPermission(permission);
    }

    private boolean checkIGPerm(CommandSender sender)
    {
        WarUser user = users.getUser(sender);
        Group team = user.getTeam();
        if (team.getKey() == 0)
        {
            return !config.IGPerm_user.contains(this.permission);
        }
        if (team.isUser(user))
        {
            return !config.IGPerm_member.contains(this.permission);
        }
        if (team.isMod(user))
        {
            return !config.IGPerm_mod.contains(this.permission);
        }
        if (team.isAdmin(user))
        {
            return !config.IGPerm_leader.contains(this.permission);
        }
        return false;
    }

    private boolean checkBypass(CommandSender sender)
    {
        if (users.getUser(sender).hasBypass(permission))
        {
            return true;
        }
        return false;
    }

    public boolean hasPerm(CommandSender sender)
    {
        if (this.name().endsWith("_BP"))
        {
            if (this.checkBypass(sender))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        if (this.checkPerm(sender))
        {
            if (this.checkIGPerm(sender))
            {
                return true;
            }
        }
        this.send(sender);
        return false;
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

    /**
     * @return the permission
     */
    public String getPermText()
    {
        return permission;
    }
}
