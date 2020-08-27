/*
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
package org.cubeengine.libcube.service.i18n;

import static org.cubeengine.dirigent.context.Contexts.LOCALE;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.cubeengine.dirigent.builder.MessageBuilder;
import org.cubeengine.dirigent.context.Context;
import org.cubeengine.dirigent.parser.component.Component;
import org.cubeengine.dirigent.parser.component.UnresolvableMacro;
import org.cubeengine.i18n.I18nService;
import org.cubeengine.libcube.service.i18n.formatter.component.ClickComponent;
import org.cubeengine.libcube.service.i18n.formatter.component.HoverComponent;
import org.cubeengine.libcube.service.i18n.formatter.component.StyledComponent;

import java.util.Locale;

public class TextMessageBuilder extends MessageBuilder<net.kyori.adventure.text.Component, TextComponent.Builder>
{

    private final I18nService i18n;

    public TextMessageBuilder(I18nService i18n)
    {
        this.i18n = i18n;
    }

    @Override
    public TextComponent.Builder newBuilder()
    {
        return TextComponent.builder();
    }

    @Override
    public net.kyori.adventure.text.Component finalize(TextComponent.Builder textBuilder, Context context)
    {
        return textBuilder.build();
    }

    @Override
    public void buildText(org.cubeengine.dirigent.parser.component.TextComponent component, TextComponent.Builder builder, Context context)
    {
        builder.append(TextComponent.of(component.getText()));
    }

    @Override
    public void buildOther(Component component, TextComponent.Builder builder, Context context)
    {
        if (component instanceof StyledComponent)
        {
            buildStyled((StyledComponent)component, builder, context);
        }
        else if (component instanceof HoverComponent)
        {
            buildHover(((HoverComponent)component), builder, context);
        }
        else if (component instanceof ClickComponent)
        {
            buildClick(((ClickComponent)component), builder, context);
        }
        else if (component instanceof org.cubeengine.libcube.service.i18n.formatter.component.TextComponent)
        {
            builder.append(((org.cubeengine.libcube.service.i18n.formatter.component.TextComponent) component).getText());
        }
        else
        {
            throw new IllegalArgumentException("Unknown Component:" + component.toString());
        }
    }

    private void buildClick(ClickComponent click, TextComponent.Builder builder, Context context)
    {
        TextComponent.Builder b = TextComponent.builder();
        buildAny(click.getComponent(), b, context);
        final ClickEvent clickEvent = click.getClick();
        b.clickEvent(clickEvent);
        builder.append(b.build());
    }

    private void buildHover(HoverComponent hover, TextComponent.Builder builder, Context context)
    {
        TextComponent.Builder b = TextComponent.builder();
        buildAny(hover.getComponent(), b, context);
        b.hoverEvent(hover.getHoverEvent());
        builder.append(b.build());
    }

    private void buildStyled(StyledComponent styled, TextComponent.Builder builder, Context context)
    {
        TextComponent.Builder b = TextComponent.builder();
        buildAny(styled.getComponent(), b, context);
        b.style(styled.getFormat());
        builder.append(b.build());
    }

    @Override
    public void buildUnresolvable(UnresolvableMacro component, TextComponent.Builder builder, Context context)
    {
        TextComponent.Builder b = TextComponent.builder();
        Locale locale = context.get(LOCALE);
        b.append(TextComponent.of(i18n.translate(locale, "{{MISSING MACRO}}")).color(NamedTextColor.DARK_RED));
        b.hoverEvent(HoverEvent.showText(TextComponent.of(i18n.translate(locale, "Please report this!"))));
        builder.append(b.build());
    }
}
