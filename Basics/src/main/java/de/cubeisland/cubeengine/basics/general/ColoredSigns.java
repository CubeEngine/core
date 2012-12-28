package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class ColoredSigns implements Listener
{
    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        if (BasicsPerm.SIGN_COLORED.isAuthorized(event.getPlayer()))
        {
            String[] lines = event.getLines();
            for (int i = 0; i < 4; ++i)
            {
                lines[i] = ChatFormat.parseFormats(lines[i]);
            }
            for (int i = 0; i < 4; ++i)
            {
                event.setLine(i, lines[i]);
            }
        }
    }
}
