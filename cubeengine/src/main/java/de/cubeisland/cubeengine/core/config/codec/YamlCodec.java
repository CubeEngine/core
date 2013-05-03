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
package de.cubeisland.cubeengine.core.config.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.node.IntNode;
import de.cubeisland.cubeengine.core.config.node.ListNode;
import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.NullNode;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.util.convert.Convert;

import org.yaml.snakeyaml.Yaml;

/**
 * This class acts as a codec for yaml-configurations.
 */
public class YamlCodec extends MultiConfigurationCodec
{
    private final Yaml yaml;

    private volatile boolean mapEnd = false;

    public YamlCodec()
    {
        super();
        this.yaml = new Yaml();

        COMMENT_PREFIX = "# ";
        OFFSET = "  ";
        LINE_BREAK = "\n";
        QUOTE = "'";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadFromInputStream(CodecContainer container, InputStream is)
    {
        if (is == null)
        {
            container.values = MapNode.emptyMap(); // InputStream null -> config was not existent
            return;
        }
        Map<Object, Object> map = (Map<Object, Object>)yaml.load(is);
        if (map == null)
        {
            container.values = MapNode.emptyMap(); // loadValues null -> config exists but was empty
        }
        else
        {
            container.values = (MapNode)Convert.wrapIntoNode(map);
            Node revisionNode = container.values.getNodeAt("revision", PATH_SEPARATOR);
            if (!(revisionNode instanceof NullNode))
            {
                if (revisionNode instanceof IntNode)
                {
                    container.revision = ((IntNode)revisionNode).getValue();
                }
                else
                {
                    CubeEngine.getLog().log(LogLevel.WARNING, "Invalid revision in a configuration!");
                }
            }
        }
    }

    private boolean needsQuote(String s)
    {
        return (s.startsWith("#") || s.contains(" #") || s.startsWith("@")
                || s.startsWith("`") || s.startsWith("[") || s.startsWith("]")
                || s.startsWith("{") || s.startsWith("}") || s.startsWith("|")
                || s.startsWith(">") || s.startsWith("!") || s.startsWith("%")
                || s.endsWith(":") || s.startsWith("- ") || s.startsWith(",")
                || s.contains("&")
                || s.matches("[0-9]+:[0-9]+")) || s.isEmpty() || s.equals("*");
    }

    @Override
    public void convertMap(OutputStreamWriter writer, CodecContainer container, MapNode values) throws IOException
    {
        mapEnd = false;
        this.convertMap(writer, container, values, 0, false);
    }

    /**
     * Serializes a single value
     *
     * @param container the codec-container
     * @param value the value at given path
     * @param off the current offset
     * @param inCollection
     * @return
     */
    private void convertValue(OutputStreamWriter writer, CodecContainer container, Node value, int off, boolean inCollection) throws IOException
    {
        mapEnd = false;
        StringBuilder sb = new StringBuilder();
        String offset = this.offset(off);
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
                else if (this.needsQuote(string))
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
                this.convertMap(writer, container, ((MapNode)value), off + 1, inCollection);
                return;
            }
            else if (value instanceof ListNode) // List-Node? -> list the nodes
            {
                if (((ListNode)value).isEmpty())
                {
                    sb.append("[]").toString();
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
                        this.convertMap(writer, container, (MapNode)listedNode, off + 2, true);
                    }
                    else
                    {
                        this.convertValue(writer, container, listedNode, off + 1, true);
                    }
                }
                mapEnd = true;
                first = false;
                return;
            }
            else
            {
                writer.append(value.unwrap());
            }
        }
        this.first = false;
        writer.append(LINE_BREAK);
    }

    /**
     * Serializes the values in the map
     *
     * @param container the codec-container
     * @param values the values at given path
     * @param off the current offset
     * @param inCollection
     * @return  the serialized value
     */
    private void convertMap(OutputStreamWriter writer, CodecContainer container, MapNode values, int off, boolean inCollection) throws IOException
    {
        Map<String, Node> map = values.getMappedNodes();
        if (map.isEmpty())
        {
            writer.append(this.offset(off)).append("{}").append(LINE_BREAK);
            return;
        }
        for (Entry<String, Node> entry : map.entrySet())
        {
            if (mapEnd && !inCollection)
            {
                writer.append(LINE_BREAK);
            }
            StringBuilder sb = new StringBuilder();
            String path = entry.getValue().getPath(PATH_SEPARATOR);
            String comment = this.buildComment(container, path, off);
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
                sb.append(this.offset(off)); // Map in collection first does not get offset
            }
            sb.append(values.getOriginalKey(Node.getSubKey(path, PATH_SEPARATOR))).append(": ");
            writer.append(sb.toString());
            this.convertValue(writer, container, entry.getValue(), off, false);
        }
        mapEnd = true;
        first = true;
    }

    @Override
    public String buildComment(CodecContainer container, String path, int off)
    {
        String comment = container.getComment(path);
        if (comment == null)
        {
            return ""; //No Comment
        }
        String offset = this.offset(off);
        comment = comment.replace(LINE_BREAK, LINE_BREAK + offset + COMMENT_PREFIX); // multi line
        comment = offset + COMMENT_PREFIX + comment + LINE_BREAK;
        return comment;
    }

    @Override
    public String getExtension()
    {
        return "yml";
    }

    @Override
    protected CodecContainer createCodecContainer()
    {
        return new YamlCodecContainer(this);
    }
}
