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

import org.cubeengine.dirigent.Component;
import org.cubeengine.dirigent.parser.component.Text;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.HoverAction;

public class HoverComponent implements Component
{
    private final Object hover;
    private final Component component;

    private HoverComponent(Object hover, Component component)
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

    public Component getComponent()
    {
        return component;
    }

    public static Component hoverAchievment(Achievement achievement, Component component)
    {
        return new HoverComponent(achievement, component);
    }

    public static Component hoverItem(ItemStack item, Component component)
    {
        return new HoverComponent(item, component);
    }

    public static Component hoverEntity(Entity entity, String name, Component component)
    {
        return new HoverComponent(new HoverAction.ShowEntity.Ref(entity, name), component);
    }

    public static Component hoverText(org.spongepowered.api.text.Text text, Component component)
    {
        return new HoverComponent(text, component);
    }

    public static Component hoverText(String text, Component component)
    {
        return new HoverComponent(Texts.of(text), component);
    }

    public static Component hoverAchievment(Achievement achievement, String component)
    {
        return new HoverComponent(achievement, component);
    }

    public static Component hoverItem(ItemStack item, String component)
    {
        return new HoverComponent(item, component);
    }

    public static Component hoverEntity(Entity entity, String name, String component)
    {
        return new HoverComponent(new HoverAction.ShowEntity.Ref(entity, name), component);
    }

    public static Component hoverText(org.spongepowered.api.text.Text text, String component)
    {
        return new HoverComponent(text, component);
    }

    public static Component hoverText(String text, String component)
    {
        return new HoverComponent(Texts.of(text), component);
    }
}
