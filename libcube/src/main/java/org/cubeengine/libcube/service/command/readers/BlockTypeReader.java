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
package org.cubeengine.libcube.service.command.readers;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;

import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.ReaderException;
import org.cubeengine.libcube.service.command.TranslatedReaderException;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlockTypeReader implements ArgumentReader<BlockType>, Completer
{
    private I18n i18n;
    private static final String MINECRAFT = "minecraft:";

    public BlockTypeReader(I18n i18n)
    {
        this.i18n = i18n;
    }

    @Override
    public BlockType read(Class aClass, CommandInvocation invocation) throws ReaderException
    {
        String arg = invocation.consume(1);
        BlockType item = Sponge.getRegistry().getType(BlockType.class, arg.toLowerCase()).orElse(null);
        if (item == null)
        {
            throw new TranslatedReaderException(i18n.getTranslation(invocation.getContext(Locale.class), NEGATIVE, "ItemType {input#item} not found!", arg));
        }
        return item;
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        ArrayList<String> list = new ArrayList<>();
        String token = invocation.currentToken().toLowerCase();
        if (MINECRAFT.startsWith(token))
        {
            list.add(MINECRAFT);
        }
        boolean startMc = token.startsWith(MINECRAFT);
        for (BlockType type : Sponge.getRegistry().getAllOf(BlockType.class))
        {
            if (type.getId().startsWith(token))
            {
                if (!type.getId().startsWith(MINECRAFT) || startMc)
                {
                    list.add(type.getId());
                }
            }
            if (type.getId().startsWith(MINECRAFT + token))
            {
                list.add(type.getId().substring(MINECRAFT.length()));
            }
        }
        return list;
    }
}
