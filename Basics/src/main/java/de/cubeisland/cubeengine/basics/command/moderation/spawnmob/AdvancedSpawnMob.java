package de.cubeisland.cubeengine.basics.command.moderation.spawnmob;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.user.User;
import gnu.trove.set.hash.TLongHashSet;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AdvancedSpawnMob implements Listener
{
    private TLongHashSet userInMode = new TLongHashSet();
    private Basics module;

    private void enterMode(User user)
    {
        this.userInMode.add(user.key);
    }

    private void exitMode(User user)
    {
        this.userInMode.remove(user.key);
    }

    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        User user = this.module.getUserManager().getExactUser(event.getPlayer());
        if (this.userInMode.contains(user.key))
        {
            if (event.getMessage().equalsIgnoreCase("exit"))
            {
                this.exitMode(user);
                return;
            }
        }
        for (long userKey : this.userInMode.toArray())
        {
            User ignoreChatUser = this.module.getUserManager().getUser(userKey);
            event.getRecipients().remove(ignoreChatUser.getPlayer());
            //TODO save chat for this player (up to ~50lines)
        }
    }
}
