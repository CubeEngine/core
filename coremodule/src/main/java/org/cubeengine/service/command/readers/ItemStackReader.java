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
package org.cubeengine.service.command.readers;

import java.util.Locale;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import org.cubeengine.service.command.TranslatedReaderException;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.module.core.util.matcher.MaterialMatcher;
import org.spongepowered.api.item.inventory.ItemStack;

import static org.cubeengine.service.i18n.formatter.MessageType.NEGATIVE;

public class ItemStackReader implements ArgumentReader<ItemStack>
{
    private MaterialMatcher materialMatcher;
    private I18n i18n;

    public ItemStackReader(MaterialMatcher materialMatcher, I18n i18n)
    {
        this.materialMatcher = materialMatcher;
        this.i18n = i18n;
    }

    @Override
    public ItemStack read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String arg = invocation.consume(1);
        ItemStack item = materialMatcher.itemStack(arg);
        if (item == null)
        {
            throw new TranslatedReaderException(i18n.translate(invocation.getContext(Locale.class), NEGATIVE, "Item {input#item} not found!", arg));
        }
        return item;
    }
}
