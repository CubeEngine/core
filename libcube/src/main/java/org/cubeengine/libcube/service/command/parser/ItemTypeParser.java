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
package org.cubeengine.libcube.service.command.parser;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;

import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.argument.Completer;
import org.cubeengine.butler.parameter.argument.ArgumentParser;
import org.cubeengine.butler.parameter.argument.ParserException;
import org.cubeengine.libcube.service.command.TranslatedParserException;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemTypeParser implements ArgumentParser<ItemType>, Completer
{
    private static final String MINECRAFT = "minecraft:";
    private I18n i18n;

    public ItemTypeParser(I18n i18n)
    {
        this.i18n = i18n;
    }

    @Override
    public ItemType parse(Class aClass, CommandInvocation invocation) throws ParserException
    {
        String arg = invocation.consume(1);
        ItemType item = Sponge.getRegistry().getType(ItemType.class, arg.toLowerCase()).orElse(null);
        if (item == null)
        {
            throw new TranslatedParserException(i18n.translate(invocation.getContext(Locale.class), NEGATIVE, "ItemType {input#block} not found!", arg));
        }
        return item;
    }

    @Override
    public List<String> suggest(Class type, CommandInvocation invocation)
    {
        ArrayList<String> list = new ArrayList<>();
        String token = invocation.currentToken().toLowerCase();
        if (MINECRAFT.startsWith(token))
        {
            list.add(MINECRAFT);
        }
        boolean startMc = token.startsWith(MINECRAFT);
        for (ItemType iType : Sponge.getRegistry().getAllOf(ItemType.class))
        {
            if (iType.getId().startsWith(token))
            {
                if (!iType.getId().startsWith(MINECRAFT) || startMc)
                {
                    list.add(iType.getId());
                }
            }
            if (iType.getId().startsWith(MINECRAFT + token))
            {
                list.add(iType.getId().substring(MINECRAFT.length()));
            }
        }
        return list;
    }
}
