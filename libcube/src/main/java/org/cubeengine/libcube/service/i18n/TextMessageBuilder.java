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

import java.net.URL;
import java.util.Locale;

import de.cubeisland.engine.i18n.I18nService;
import org.cubeengine.dirigent.builder.MessageBuilder;
import org.cubeengine.dirigent.context.Context;
import org.cubeengine.dirigent.parser.component.UnresolvableMacro;
import org.cubeengine.dirigent.parser.component.Component;
import org.cubeengine.libcube.service.i18n.formatter.component.ClickComponent;
import org.cubeengine.libcube.service.i18n.formatter.component.HoverComponent;
import org.cubeengine.libcube.service.i18n.formatter.component.StyledComponent;
import org.cubeengine.libcube.service.i18n.formatter.component.TextComponent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.HoverAction.ShowEntity;

import static org.cubeengine.dirigent.context.Contexts.LOCALE;
import static org.spongepowered.api.text.action.TextActions.*;
import static org.spongepowered.api.text.format.TextColors.DARK_RED;

public class TextMessageBuilder extends MessageBuilder<Text, Text.Builder>
{

    private final I18nService i18n;

    public TextMessageBuilder(I18nService i18n)
    {
        this.i18n = i18n;
    }

    @Override
    public Text.Builder newBuilder()
    {
        return Text.builder();
    }

    @Override
    public Text finalize(Text.Builder textBuilder, Context context)
    {
        return textBuilder.build();
    }

    @Override
    public void buildText(org.cubeengine.dirigent.parser.component.TextComponent component, Text.Builder builder, Context context)
    {
        builder.append(Text.of(component.getText()));
    }

    @Override
    public void buildOther(Component component, Text.Builder builder, Context context)
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
        else if (component instanceof TextComponent)
        {
            builder.append(((TextComponent) component).getText());
        }
        else
        {
            throw new IllegalArgumentException("Unknown Component:" + component.toString());
        }
    }

    private void buildClick(ClickComponent click, Text.Builder builder, Context context)
    {
        Text.Builder b = Text.builder();
        buildAny(click.getComponent(), b, context);

        Object toClick = click.getClick();
        if (toClick instanceof URL)
        {
            b.onClick(openUrl((URL)toClick));
        }
        else if (toClick instanceof String)
        {
            b.onClick(runCommand(((String)toClick)));
        }

        builder.append(b.build());
    }

    private void buildHover(HoverComponent hover, Text.Builder builder, Context context)
    {
        Text.Builder b = Text.builder();
        buildAny(hover.getComponent(), b, context);

        Object toHover = hover.getHover();
        // TODO advancements
        /*
        if (toHover instanceof Achievement)
        {
            b.onHover(showAchievement(((Achievement)toHover)));
        }
        else */
        if (toHover instanceof ItemStack)
        {
            b.onHover(showItem(((ItemStack)toHover).createSnapshot()));
        }
        else if (toHover instanceof ShowEntity.Ref)
        {
            b.onHover(showEntity(((ShowEntity.Ref)toHover)));
        }
        else if (toHover instanceof Text)
        {
            b.onHover(showText(((Text)toHover)));
        }
        builder.append(b.build());
    }

    private void buildStyled(StyledComponent styled, Text.Builder builder, Context context)
    {
        Text.Builder b = Text.builder();
        buildAny(styled.getComponent(), b, context);
        b.format(styled.getFormat());
        builder.append(b.build());
    }

    @Override
    public void buildUnresolvable(UnresolvableMacro component, Text.Builder builder, Context context)
    {
        Text.Builder b = Text.builder();
        Locale locale = context.get(LOCALE);
        b.append(Text.of(DARK_RED, i18n.translate(locale, "{{MISSING MACRO}}")));
        b.onHover(showText(Text.of(i18n.translate(locale, "Please report this!"))));
        builder.append(b.build());
    }
}
