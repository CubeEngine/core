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
package org.cubeengine.libcube.service.config;

import org.cubeengine.converter.ConversionException;
import org.cubeengine.converter.converter.SimpleConverter;
import org.cubeengine.converter.node.IntNode;
import org.cubeengine.converter.node.Node;
import org.cubeengine.converter.node.StringNode;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationConverter extends SimpleConverter<Duration>
{
    private Pattern pattern = Pattern.compile("([0-9]*D)?((?:[0-9]*H)?(?:[0-9]*M)?(?:[0-9]*S)?)");

    @Override
    public Node toNode(Duration d) throws ConversionException
    {
        long days = d.toDays();
        long hours = d.minusDays(days).toHours();
        long minutes = d.minusDays(days).minusHours(hours).toMinutes();
        long seconds = d.minusDays(days).minusHours(hours).minusMinutes(minutes).toMillis() / 1000;
        return StringNode.of(days + "D" + hours + "H" + minutes + "M" + seconds + "S");
    }

    @Override
    public Duration fromNode(Node node) throws ConversionException
    {
        try
        {
            if (node instanceof IntNode)
            {
                return Duration.ofMillis(((IntNode)node).getValue().longValue());
            }
            Matcher matcher = pattern.matcher(node.asText().toUpperCase());
            String text = matcher.replaceFirst("P$1T$2");
            return Duration.parse(text);
        }
        catch (Exception e)
        {
            throw ConversionException.of(this, node, "Unknown error while parsing Duration!", e);
        }
    }
}
