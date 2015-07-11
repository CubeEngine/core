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

import de.cubeisland.engine.messagecompositor.parser.component.MessageComponent;
import de.cubeisland.engine.messagecompositor.parser.component.Text;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.format.BaseFormatting;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;

public class HoverComponent implements MessageComponent
{
    private final Object hover;
    private final MessageComponent component;

    private HoverComponent(Object hover, MessageComponent component)
    {
        this.hover = hover;
        this.component = component;
    }

    public HoverComponent(Object hover, String text)
    {
        this(hover, new Text(text));
    }

    public Object getHover()
    {
        return hover;
    }

    public MessageComponent getComponent()
    {
        return component;
    }

    public static MessageComponent hoverAchievment(Achievement achievement, MessageComponent component)
    {
        return new HoverComponent(achievement, component);
    }

    public static MessageComponent hoverItem(ItemStack item, MessageComponent component)
    {
        return new HoverComponent(item, component);
    }

    public static MessageComponent hoverEntity(Entity entity, String name, MessageComponent component)
    {
        return new HoverComponent(new HoverAction.ShowEntity.Ref(entity, name), component);
    }

    public static MessageComponent hoverText(org.spongepowered.api.text.Text text, MessageComponent component)
    {
        return new HoverComponent(text, component);
    }

    public static MessageComponent hoverText(String text, MessageComponent component)
    {
        return new HoverComponent(Texts.of(text), component);
    }

    public static MessageComponent hoverAchievment(Achievement achievement, String component)
    {
        return new HoverComponent(achievement, component);
    }

    public static MessageComponent hoverItem(ItemStack item, String component)
    {
        return new HoverComponent(item, component);
    }

    public static MessageComponent hoverEntity(Entity entity, String name, String component)
    {
        return new HoverComponent(new HoverAction.ShowEntity.Ref(entity, name), component);
    }

    public static MessageComponent hoverText(org.spongepowered.api.text.Text text, String component)
    {
        return new HoverComponent(text, component);
    }

    public static MessageComponent hoverText(String text, String component)
    {
        return new HoverComponent(Texts.of(text), component);
    }
}
