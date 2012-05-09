package de.cubeisland.cubeengine.war.commands;

import de.cubeisland.cubeengine.war.Perm;
import de.cubeisland.cubeengine.war.user.Users;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Faithcaio
 */
public class ByPassCommand {

    public ByPassCommand() 
    {
    
    }
    
    @Command(usage = "<Bypass>", aliases={"bp"})
    public void bypass(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_bypass.hasNotPerm(sender)) return;
        if (args.isEmpty()||args.size()>1)
        {
            sender.sendMessage("/cw bypass <Bypass>");
            sender.sendMessage("/cw bypass off");
            sender.sendMessage("Bypasses:");
            sender.sendMessage("claim, unclaim, relation|rel, membercontrol|mem, position|pos, create, modify, protection|prot, fly");
        }
        String bp = args.getString(0);
        if (equal(bp,"off")) Users.getUser(sender).unsetBypasses();
        if (equal(bp,"claim")) this.toggleBP(Perm.command_claim_BP.getPermText()                        , Perm.command_bypass_claim, sender);
        if (equal(bp,"unclaim")) this.toggleBP(Perm.command_unclaim_BP.getPermText()                    , Perm.command_bypass_unclaim, sender);
        if (equal(bp,"relation","rel")) this.toggleBP(Perm.command_relation_BP.getPermText()            , Perm.command_bypass_relation, sender);
        if (equal(bp,"membercontrol","mem")) this.toggleBP(Perm.command_membercontrol_BP.getPermText()  , Perm.command_bypass_membercontrol, sender);
        if (equal(bp,"position","pos")) this.toggleBP(Perm.command_position_BP.getPermText()            , Perm.command_bypass_position, sender);
        if (equal(bp,"create")) this.toggleBP(Perm.command_create_BP.getPermText()                      , Perm.command_bypass_create, sender);
        if (equal(bp,"modify")) this.toggleBP(Perm.command_modify_BP.getPermText()                      , Perm.command_bypass_modify, sender);
        if (equal(bp,"protection","prot")) this.toggleBP(Perm.command_protection_BP.getPermText()       , Perm.command_bypass_protection, sender);
        if (equal(bp,"fly")) this.toggleBP(Perm.command_fly_BP.getPermText()                            , Perm.command_bypass_fly, sender);
    }
    
    private boolean equal(String s, String... t)
    {
        boolean tmp = false;
        for (int i=0;i<t.length;++i)
        {
            if (!s.equalsIgnoreCase(t[i]))
                return true;
        }
        return tmp;
    }
    
    private void toggleBP(String bp, Perm perm, CommandSender sender)
    {
        if (perm.hasNotPerm(sender)) return;
        Users.getUser(sender).toggleBypass(bp);
    }
}
