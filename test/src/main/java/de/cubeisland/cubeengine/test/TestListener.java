package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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

    private void testI18n(AsyncPlayerChatEvent event)
    {
        User user = CubeEngine.getUserManager().getExactUser(event.getPlayer());
        user.sendTranslated("Your language is: %s", user.getLocale());
        user.sendTranslated("english TEST");
        user.sendTranslated("&1color &2Test");
        user.sendMessage(CubeEngine.getCore().getI18n().translate("fr_FR", "test", "&1color &2Test"));
        user.sendTranslated("NotTranslatedMessageIsNotTranslated");
    }
}