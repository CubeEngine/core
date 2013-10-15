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
public class YamlCodec extends MultiConfigurationCodec implements CommentableCodec
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
            this.convertMap(writer, node);
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

    private volatile boolean mapEnd;
    private volatile boolean first;

    /**
     * Serializes the values in the map
     *
     * @param writer the Output to write into
     */
    private void convertMap(OutputStreamWriter writer, MapNode baseNode) throws IOException
    {
        first = true;
        mapEnd = false;
        this.convertMap(writer, baseNode, 0, false);
    }

    /**
     * Serializes a single value
     *
     * @param value the value at given path
     * @param off the current offset
     * @param inCollection
     * @return
     */
    private void convertValue(OutputStreamWriter writer, Node value, int off, boolean inCollection) throws IOException
    {
        mapEnd = false;
        StringBuilder sb = new StringBuilder();
        String offset = offset(off);
        if (!(value instanceof NullNode)) // null-Node ?
        {
            if (value instanceof StringNode) // String-Node ?
            {
                String string = ((StringNode)value).getValue();
                if (string.contains(LINE_BREAK)) // MultiLine String
                {
                    sb.append("|").append(LINE_BREAK).append(offset).append(OFFSET);
                    sb.append(string.trim().replace(LINE_BREAK, LINE_BREAK + offset + OFFSET));
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
            else if (value instanceof MapNode) // Map-Node ? -> redirect
            {
                first = true;
                sb.append(LINE_BREAK);
                writer.append(sb.toString());
                this.convertMap(writer, ((MapNode)value), off + 1, inCollection);
                return;
            }
            else if (value instanceof ListNode) // List-Node? -> list the nodes
            {
                if (((ListNode)value).isEmpty())
                {
                    sb.append("[]");
                }
                writer.append(sb.toString());
                writer.append(LINE_BREAK);
                for (Node listedNode : ((ListNode)value).getListedNodes()) //Convert Collection
                {
                    if (mapEnd)
                    {
                        writer.append(LINE_BREAK);
                    }
                    writer.append(offset).append(OFFSET).append("- ");
                    if (listedNode instanceof MapNode)
                    {
                        first = true;
                        this.convertMap(writer, (MapNode)listedNode, off + 2, true);
                    }
                    else
                    {
                        this.convertValue(writer, listedNode, off + 1, true);
                    }
                }
                mapEnd = true;
                first = false;
                return;
            }
            else
            {
                writer.append(value.asText());
            }
        }
        this.first = false;
        writer.append(LINE_BREAK);
    }

    /**
     * Serializes the values in the map
     *
     * @param values the values at given path
     * @param off the current offset
     * @param inCollection
     * @return the serialized value
     */
    private void convertMap(OutputStreamWriter writer, MapNode values, int off, boolean inCollection) throws IOException
    {
        Map<String, Node> map = values.getMappedNodes();
        if (map.isEmpty())
        {
            writer.append(offset(off)).append("{}").append(LINE_BREAK);
            return;
        }
        for (Entry<String, Node> entry : map.entrySet())
        {
            if (mapEnd && !inCollection)
            {
                writer.append(LINE_BREAK);
            }
            StringBuilder sb = new StringBuilder();
            String comment = this.buildComment(entry.getValue().getComment(), off);
            if (!comment.isEmpty())
            {
                if (!first) // if not already one line free
                {
                    sb.append(LINE_BREAK); // add free line before comment
                }
                sb.append(comment);
                first = false;
            }
            if (!(first && inCollection))
            {
                sb.append(offset(off)); // Map in collection first does not get offset
            }
            sb.append(values.getOriginalKey(entry.getKey())).append(": ");
            writer.append(sb.toString());
            this.convertValue(writer, entry.getValue(), off, false);
        }
        mapEnd = true;
        first = true;
    }

    /**
     * Returns the offset as String
     *
     * @param offset the offset
     * @return the offset
     */
    private static String offset(int offset)
    {
        StringBuilder off = new StringBuilder("");
        for (int i = 0; i < offset; ++i)
        {
            off.append(OFFSET);
        }
        return off.toString();
    }

    @Override
    public String buildComment(String comment, int offset)
    {
        if (comment == null || comment.isEmpty())
        {
            return ""; //No Comment
        }
        String off = offset(offset);
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
