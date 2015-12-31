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
package org.cubeengine.service.i18n.formatter;

import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;

public class MessageType
{
    public final static TextFormat POSITIVE = TextFormat.NONE.color(TextColors.GREEN);
    public final static TextFormat NEUTRAL = TextFormat.NONE.color(TextColors.YELLOW);
    public final static TextFormat NEGATIVE = TextFormat.NONE.color(TextColors.RED);
    public final static TextFormat CRITICAL = TextFormat.NONE.color(TextColors.DARK_RED);
    public final static TextFormat NONE = TextFormat.NONE.color(TextColors.RESET);
}
