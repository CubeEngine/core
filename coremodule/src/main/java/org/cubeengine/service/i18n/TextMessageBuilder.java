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
package org.cubeengine.service.i18n;

import java.net.URL;
import org.cubeengine.service.i18n.formatter.component.ClickComponent;
import org.cubeengine.service.i18n.formatter.component.HoverComponent;
import org.cubeengine.service.i18n.formatter.component.StyledComponent;
import org.cubeengine.dirigent.Component;
import org.cubeengine.dirigent.builder.MessageBuilder;
import org.cubeengine.dirigent.parser.component.ErrorComponent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.HoverAction.ShowEntity;

import static org.spongepowered.api.text.action.TextActions.*;
import static org.spongepowered.api.text.format.TextColors.DARK_RED;

public class TextMessageBuilder extends MessageBuilder<Text, TextBuilder>
{

    @Override
    public TextBuilder newBuilder()
    {
        return Texts.builder();
    }

    @Override
    public Text finalize(TextBuilder textBuilder)
    {
        return textBuilder.build();
    }

    @Override
    public void build(org.cubeengine.dirigent.parser.component.Text component, TextBuilder builder)
    {
        builder.append(Texts.of(component.getString()));
    }

    @Override
    public void buildOther(Component component, TextBuilder builder)
    {
        if (component instanceof StyledComponent)
        {
            buildStyled((StyledComponent)component, builder);
        }
        else if (component instanceof HoverComponent)
        {
            buildHover(((HoverComponent)component), builder);
        }
        else if (component instanceof ClickComponent)
        {
            buildClick(((ClickComponent)component), builder);
        }

        // TODO
    }

    private void buildClick(ClickComponent click, TextBuilder builder)
    {
        TextBuilder b = Texts.builder();
        buildAny(click.getComponent(), b);

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

    private void buildHover(HoverComponent hover, TextBuilder builder)
    {
        TextBuilder b = Texts.builder();
        buildAny(hover.getComponent(), b);

        Object toHover = hover.getHover();
        if (toHover instanceof Achievement)
        {
            b.onHover(showAchievement(((Achievement)toHover)));
        }
        else if (toHover instanceof ItemStack)
        {
            b.onHover(showItem(((ItemStack)toHover)));
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

    private void buildStyled(StyledComponent styled, TextBuilder builder)
    {
        TextBuilder b = Texts.builder();
        buildAny(styled.getComponent(), b);
        b.format(styled.getFormat());
        builder.append(b.build());
    }

    @Override
    public void build(ErrorComponent component, TextBuilder builder)
    {
        TextBuilder b = Texts.builder();
        if (component instanceof org.cubeengine.dirigent.parser.component.Text)
        {
            b.append(Texts.of(DARK_RED, ((org.cubeengine.dirigent.parser.component.Text)component).getString()));
        }
        else
        {
            b.append(Texts.of(DARK_RED, "ERROR"));
        }
        b.onHover(showText(Texts.of(component.getError())));
    }
}
