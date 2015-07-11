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
package de.cubeisland.engine.service.i18n.formatter.component;

import java.net.URL;
import de.cubeisland.engine.messagecompositor.parser.component.MessageComponent;
import de.cubeisland.engine.messagecompositor.parser.component.Text;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.HoverAction;

public class ClickComponent implements MessageComponent
{
    private final Object click;
    private final MessageComponent component;

    private ClickComponent(Object hover, MessageComponent component)
    {
        this.click = hover;
        this.component = component;
    }

    public ClickComponent(Object hover, String text)
    {
        this(hover, new Text(text));
    }

    public Object getClick()
    {
        return click;
    }

    public MessageComponent getComponent()
    {
        return component;
    }

    public static MessageComponent openURL(URL url, MessageComponent component)
    {
        return new ClickComponent(url, component);
    }

    public static MessageComponent runCommand(String command, MessageComponent component)
    {
        return new ClickComponent(command, component);
    }

    public static MessageComponent openURL(URL url, String text)
    {
        return new ClickComponent(url, text);
    }

    public static MessageComponent runCommand(String command, String text)
    {
        return new ClickComponent(command, text);
    }

}
