/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.test.tests.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.test.Test;

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
        User user = CubeEngine.getUserManager().getExactUser(event.getPlayer().getUniqueId());
        user.sendTranslated(MessageType.NONE, "Your language is: {input#locale}", user.getLocale());
        user.sendTranslated(MessageType.NONE, "english TEST");
        user.sendTranslated(MessageType.NONE, "NotTranslatedMessageIsNotTranslated");
    }
}
