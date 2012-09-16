package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author Anselm Brehme
 */
public class TestListener implements Listener
{
    Test test;

    public TestListener(Test test)
    {
        this.test = test;
    }

    @EventHandler
    public void playerChat(final AsyncPlayerChatEvent event)
    {
        if (event.getMessage().startsWith("um "))
        {
            this.testUserManager(event);
        }
        if (event.getMessage().startsWith("i18n"))
        {
            this.testI18n(event);
        }
    }

    @EventHandler
    public void playerInteract(final PlayerInteractEvent event)
    {
        test.getLogger().debug("Interact!");
    }

    @EventHandler
    public void mobEggSpawn(final CreatureSpawnEvent event)
    {
        if (event.getSpawnReason().equals(SpawnReason.SPAWNER_EGG))
        {
            test.getLogger().debug("SpawnEggUse! "+event.getEntity());
        }
    }

    @EventHandler
    public void playerJoin(final PlayerJoinEvent event)
    {
        test.getLogger().debug(event.getPlayer().getName() + " joined!");
    }

    private void testUserManager(AsyncPlayerChatEvent event)
    {
        String msg = event.getMessage().substring(3);
        UserManager uM = CubeEngine.getUserManager();
        User user = uM.getUser(event.getPlayer());
        if (msg.startsWith("clear"))
        {
            uM.clean();
            uM.clear();
            user.sendMessage("Cleared DB and UM");
        }
        if (msg.startsWith("add "))
        {
            msg = msg.substring(4);
            uM.addUser(new User(msg));
            user.sendMessage("Added " + msg);
        }
        if (msg.startsWith("find "))
        {
            //search for User...
            msg = msg.substring(5);
            User founduser = uM.findUser(msg);
            if (founduser == null)
            {
                user.sendMessage("Not Found: " + msg);
            }
            else
            {
                founduser.sendMessage("Found you with " + msg);
                user.sendMessage("Found " + founduser.getName());
            }
        }
    }

    private void testI18n(AsyncPlayerChatEvent event)
    {
        User user = CubeEngine.getUserManager().getUser(event.getPlayer());

        user.sendMessage("test", "Your language is: %s", user.getLanguage());

        user.sendMessage("test", "english TEST");
        user.sendMessage("test", "&1color &2Test");
        user.sendMessage(CubeEngine.getCore().getI18n().translate("fr_FR", "test", "&1color &2Test"));
    }
}
