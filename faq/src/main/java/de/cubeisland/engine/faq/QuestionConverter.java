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
package de.cubeisland.engine.faq;

import java.util.HashMap;
import java.util.Map;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.configuration.convert.Converter;
import de.cubeisland.engine.configuration.exception.ConversionException;
import de.cubeisland.engine.configuration.node.MapNode;
import de.cubeisland.engine.configuration.node.Node;

public class QuestionConverter implements Converter<Question>
{
    @Override
    public Node toNode(Question object, ConverterManager manager) throws ConversionException
    {
        Map<String, Object> data = new HashMap<>();
        data.put("question", object.getQuestion());
        data.put("answer", object.getAnswer());
        data.put("keywords", object.getKeywords());

        return new MapNode(data);
    }

    @Override
    public Question fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            Map<String, Node> content = ((MapNode)node).getMappedNodes();
            if (!content.containsKey("question") || !content.containsKey("answer") ||!content.containsKey("keywords"))
            {
                return null;
            }
            return new Question(
                content.get("question").toString(),
                content.get("answer").toString(),
                manager.<String[]>convertFromNode(content.get("keywords"), String[].class));
        }
        return null;
    }
}
