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
package de.cubeisland.engine.core.config.codec;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.config.InvalidConfigurationException;
import de.cubeisland.engine.core.config.node.ListNode;
import de.cubeisland.engine.core.config.node.MapNode;
import de.cubeisland.engine.core.config.node.Node;
import de.cubeisland.engine.core.config.node.NullNode;
import de.cubeisland.engine.core.config.node.StringNode;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.convert.Convert;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.ReaderException;

/**
 * A Codec for YAML-Configurations allowing child-configurations
 */
public class YamlCodec extends MultiConfigurationCodec
{
    private static final String COMMENT_PREFIX = "# ";
    private static final String OFFSET = "  ";
    private static final String LINE_BREAK = "\n";
    private static final String QUOTE = "'";

    @Override
    public String getExtension()
    {
        return "yml";
    }

    @Override
    protected void saveIntoFile(Configuration config, MapNode node, Path file) throws IOException
    {
        try (OutputStream os = new FileOutputStream(file.toFile()))
        {
            OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
            if (config.head() != null)
            {
                writer.append("# ").append(StringUtils.implode("\n# ", config.head())).append(LINE_BREAK).append(LINE_BREAK);
            }
            convertNode(writer, node);
            if (config.tail() != null)
            {
                writer.append("# ").append(StringUtils.implode("\n# ", config.tail()));
            }
            writer.flush();
            writer.close();
        }
    }

    @Override
    @SuppressWarnings("unchecked cast")
    public MapNode loadFromInputStream(InputStream is)
    {
        MapNode values;
        try
        {
            if (is == null)
            {
                values = MapNode.emptyMap(); // InputStream null -> config was not existent
                return values;
            }
            Map<Object, Object> map = (Map<Object, Object>)new Yaml().load(is);
            if (map == null)
            {
                values = MapNode.emptyMap(); // loadValues null -> config exists but was empty
            }
            else
            {
                values = (MapNode)Convert.wrapIntoNode(map);
            }
        }
        catch (ReaderException ex)
        {
            throw new InvalidConfigurationException("Failed to parse the YAML configuration. Try encoding it as UTF-8 or validate on yamllint.com", ex);
        }
        return values;
    }

    /**
     * Serializes the values in the map
     *
     * @param writer the Output to write into
     */
    private static void convertNode(OutputStreamWriter writer, MapNode baseNode) throws IOException
    {
        convertMapNode(writer, baseNode, 0, false);
    }

    /**
     * Serializes a single value
     *
     * @param value the value at given path
     * @param offset the current offset
     * @param inCollection
     */
    private static void convertValue(OutputStreamWriter writer, Node value, int offset, boolean inCollection) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        if (!(value instanceof NullNode)) // null-Node ?
        {
            if (value instanceof StringNode) // String-Node ?
            {
                String string = ((StringNode)value).getValue();
                if (string.contains(LINE_BREAK)) // MultiLine String
                {
                    String offsetString = getOffset(offset);
                    sb.append("|").append(LINE_BREAK).append(offsetString).append(OFFSET);
                    sb.append(string.trim().replace(LINE_BREAK, LINE_BREAK + offsetString + OFFSET));
                }
                else if (needsQuote(string))
                {
                    sb.append(QUOTE).append(string).append(QUOTE);
                }
                else
                {
                    sb.append(string);
                }
                writer.append(sb.toString());
            }
            else
            {
                writer.append(value.asText());
            }
        }
        writer.append(LINE_BREAK);
    }

    /**
     * Serializes the values in the map
     *
     * @param values the values at given path
     * @param offset the current offset
     * @param inCollection
     */
    private static void convertMapNode(OutputStreamWriter writer, MapNode values, int offset, boolean inCollection) throws IOException
    {
        Map<String, Node> map = values.getMappedNodes();
        if (map.isEmpty())
        {
            if (!inCollection)
            {
                writer.append(getOffset(offset));
            }
            writer.append("{}").append(LINE_BREAK);
            return;
        }
        boolean endOfMapOrList = false;
        boolean first = true;
        for (Entry<String, Node> entry : map.entrySet())
        {
            boolean hasLine = false;
            if (endOfMapOrList && !inCollection)
            {
                writer.append(LINE_BREAK);
                hasLine = true;
            }
            StringBuilder sb = new StringBuilder();
            String comment = buildComment(entry.getValue().getComment(), offset);
            if (!comment.isEmpty())
            {
                if (!hasLine && !first) // if not already one line free
                {
                    sb.append(LINE_BREAK); // add free line before comment
                }
                sb.append(comment);
            }

            if (!(first && inCollection))
            {
                sb.append(getOffset(offset)); // Map in collection first does not get offset
            }
            sb.append(values.getOriginalKey(entry.getKey())).append(": ");
            writer.append(sb.toString());
            // Now convert the value
            if (entry.getValue() instanceof MapNode) // Map-Node?
            {
                writer.append(LINE_BREAK);
                convertMapNode(writer, ((MapNode)entry.getValue()), offset + 1, inCollection);
                endOfMapOrList = true;
            }
            else if (entry.getValue() instanceof ListNode) // List-Node? -> list the nodes
            {
                if (((ListNode)entry.getValue()).isEmpty())
                {
                    writer.append("[]").append(LINE_BREAK);
                }
                else
                {
                    convertListNode(writer, (ListNode)entry.getValue(), offset);
                }
                endOfMapOrList = true;
            }
            else // Other Node (list / normal)
            {
                convertValue(writer, entry.getValue(), offset, false);
                endOfMapOrList = false;
            }
            first = false;
        }
    }

    private static void convertListNode(OutputStreamWriter writer, ListNode value, int offset) throws IOException
    {
        writer.append(LINE_BREAK);
        boolean endOfMap = false;
        for (Node listedNode : value.getListedNodes()) //Convert Collection
        {
            if (endOfMap)
            {
                writer.append(LINE_BREAK);
            }
            writer.append(getOffset(offset)).append(OFFSET).append("- ");
            if (listedNode instanceof MapNode)
            {
                convertMapNode(writer, (MapNode)listedNode, offset + 2, true);
                endOfMap = true;
            }
            else if (listedNode instanceof ListNode)
            {
                convertListNode(writer, (ListNode)listedNode, offset);
                endOfMap = true;
            }
            else
            {
                convertValue(writer, listedNode, offset + 1, true);
                endOfMap = false;
            }
        }
    }

    /**
     * Returns the offset as String
     *
     * @param offset the offset
     * @return the offset
     */
    private static String getOffset(int offset)
    {
        StringBuilder off = new StringBuilder("");
        for (int i = 0; i < offset; ++i)
        {
            off.append(OFFSET);
        }
        return off.toString();
    }

    private static String buildComment(String comment, int offset)
    {
        if (comment == null || comment.isEmpty())
        {
            return ""; //No Comment
        }
        String off = getOffset(offset);
        comment = comment.replace(LINE_BREAK, LINE_BREAK + off + COMMENT_PREFIX); // multi line
        comment = off + COMMENT_PREFIX + comment + LINE_BREAK;
        return comment;
    }

    private static boolean needsQuote(String s)
    {
        return (s.startsWith("#") || s.contains(" #") || s.startsWith("@")
            || s.startsWith("`") || s.startsWith("[") || s.startsWith("]")
            || s.startsWith("{") || s.startsWith("}") || s.startsWith("|")
            || s.startsWith(">") || s.startsWith("!") || s.startsWith("%")
            || s.endsWith(":") || s.startsWith("- ") || s.startsWith(",")
            || s.contains("&")
            || s.matches("[0-9]+:[0-9]+")) || s.isEmpty() || s.equals("*");
    }
}
