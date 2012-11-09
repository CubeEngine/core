package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
        if (event.getMessage().startsWith("i18n"))
        {
            this.testI18n(event);
        }
    }

    @EventHandler
    public void playerInteract(final PlayerInteractEvent event)
    {
    }
    //TODO this as cmd or smth else

    private void testI18n(AsyncPlayerChatEvent event)
    {
        User user = CubeEngine.getUserManager().getExactUser(event.getPlayer());

        user.sendMessage("test", "Your language is: %s", user.getLanguage());

        user.sendMessage("test", "english TEST");
        user.sendMessage("test", "&1color &2Test");
        user.sendMessage(CubeEngine.getCore().getI18n().
            translate("fr_FR", "test", "&1color &2Test"));
        user.sendMessage("test", "NotTranslatedMessageIsNotTranslated");
    }
}
