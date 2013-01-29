package de.cubeisland.cubeengine.core.config.codec;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.ConfigurationCodec;
import de.cubeisland.cubeengine.core.config.node.*;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class acts as a codec for yaml-configurations.
 */
public class YamlCodec extends ConfigurationCodec
{
    private final Yaml yaml;

    public YamlCodec()
    {
        super();
        this.yaml = new Yaml();

        COMMENT_PREFIX = "# ";
        OFFSET = "  ";
        LINE_BREAK = "\n";
        QUOTE = "'";
    }

    //TODO \n in Strings do get lost when restarting
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
            Node revisionNode = container.values.getNodeAt("revision",PATH_SEPARATOR);
            if (! (revisionNode instanceof NullNode))
            {
                if (revisionNode instanceof IntNode)
                {
                    container.revision = ((IntNode)revisionNode).getValue();
                }
                else
                {
                    CubeEngine.getLogger().log(LogLevel.WARNING, "Invalid revision in a configuration!");
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
    @SuppressWarnings("unchecked")
    public String convertValue(CodecContainer container, Node value, int off, boolean inCollection)
    {
        StringBuilder sb = new StringBuilder();
        String offset = this.offset(off);
        if (! (value instanceof NullNode)) // null-Node ?
        {
            if (value instanceof StringNode) // String-Node ?
            {
                String string = ((StringNode)value).getValue();
                if (this.needsQuote(string))
                {
                    sb.append(QUOTE).append(string).append(QUOTE);
                }
                else
                {
                    sb.append(string);
                }
            }
            else if (value instanceof MapNode) // Map-Node ? -> redirect
            {
                first = true;
                if (!inCollection)
                {
                    sb.append(LINE_BREAK);
                }
                sb.append(this.convertMap(container,((MapNode)value), off + 1, inCollection));
                if (!sb.toString().endsWith(LINE_BREAK+LINE_BREAK))
                {
                    sb.append(LINE_BREAK);
                }
            }
            else  if (value instanceof ListNode) // List-Node? -> list the nodes
            {
                if (((ListNode)value).isEmpty())
                {
                    sb.append("[]").toString();
                }
                for (Node listedNode : ((ListNode)value).getListedNodes()) //Convert Collection
                {
                    if (!sb.toString().endsWith(LINE_BREAK+LINE_BREAK))
                    {
                        sb.append(LINE_BREAK);
                    }
                    sb.append(offset).append(OFFSET).append("- ");
                    if (listedNode instanceof MapNode)
                    {
                        first = true;
                        sb.append(this.convertMap(container, (MapNode)listedNode, off + 2, true));
                        if (!sb.toString().endsWith(LINE_BREAK+LINE_BREAK))
                        {
                            sb.append(LINE_BREAK);
                        }
                    }
                    else
                    {
                        sb.append(this.convertValue(container, listedNode, off +1, true));
                    }
                }
            }
            else
            {
                sb.append(value.unwrap());
            }
        }
        this.first = false;
        if (!inCollection)
        {
            if (!sb.toString().endsWith(LINE_BREAK+LINE_BREAK))
            {
                sb.append(LINE_BREAK);
            }
        }
        return sb.toString();
    }

    @Override
    public String convertMap(CodecContainer container, MapNode values, int off, boolean inCollection)
    {
        StringBuilder sb = new StringBuilder();
        Map<String,Node> map = values.getMappedNodes();
        if (map.isEmpty())
        {
            return sb.append(this.offset(off)).append("{}").toString();
        }
        for (Entry<String,Node> entry : map.entrySet())
        {
            String path = entry.getValue().getPath(PATH_SEPARATOR);
            String comment = this.buildComment(container, path, off);
            if (!comment.isEmpty())
            {
                if (!first && !sb.toString().endsWith(LINE_BREAK + LINE_BREAK)) // if not already one line free
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
            sb.append(this.convertValue(container, entry.getValue(), off, false));

        }
        first = true;
        return sb.toString();
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
}
