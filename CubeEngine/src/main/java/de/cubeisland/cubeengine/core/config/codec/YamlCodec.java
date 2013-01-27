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
        if (value instanceof MapNode)
        {
            if (!inCollection) //do not append offset when converting a value in a list
            {
                sb.append(offset);
            }
            Map<String,Node> map = ((MapNode)value).getMappedNodes();
            for (Entry<String,Node> entry : map.entrySet())
            {
                sb.append(entry.getKey()).append(":");
                if (entry.getValue() instanceof NullNode)
                {
                    sb.append(" ");
                }
                else
                {
                    if (value instanceof MapNode)
                    {
                        sb.append(LINE_BREAK);
                        sb.append(this.convertMap(container,entry.getValue(), off + 1, inCollection));
                        return sb.toString();
                    }
                    else
                    {
                        if (value instanceof StringNode)
                        {
                            sb.append(" ");
                            String string = ((StringNode)value).getValue();
                            if (this.needsQuote(string))
                            {
                                sb.append(QUOTE).append(string).append(QUOTE);
                            }
                            else
                            {
                                sb.append(value.toString());
                            }
                        }
                        else
                        {
                            if (value instanceof ListNode)
                            {
                                if (((ListNode)value).isEmpty())
                                {
                                    return sb.append(" []").append(LINE_BREAK).toString();
                                }
                                for (Node listedNode : ((ListNode)value).getListedNodes()) //Convert Collection
                                {
                                    sb.append(LINE_BREAK).append(offset).append("- ");
                                    if (listedNode instanceof StringNode)
                                    {
                                        String string = ((StringNode)value).getPath(PATH_SEPARATOR);
                                        if (this.needsQuote(string))
                                        {
                                            sb.append(QUOTE).append(string).append(QUOTE);
                                        }
                                        else
                                        {
                                            sb.append(string);
                                        }
                                    }
                                    else if (listedNode instanceof MapNode)
                                    {
                                        sb.append(this.convertMap(container, listedNode, off + 1, true));
                                    }
                                    else
                                    {
                                        sb.append(listedNode.unwrap());
                                    }
                                }
                            }
                            else
                            {
                                sb.append(" ").append(value.unwrap());
                            }
                        }
                    }
                }
            }
            if (!inCollection && !sb.toString().endsWith(LINE_BREAK + LINE_BREAK)) // if in collection the collection will append its linebreak
            {
                sb.append(LINE_BREAK);
            }
            this.first = false;
            return sb.toString();
        }
        else
        {
            throw new IllegalStateException("Error Something went wrong!");
        }
    }

    @Override
    public String convertMap(CodecContainer container, Node values, int off, boolean inCollection)
    {
        StringBuilder sb = new StringBuilder();
        if (values instanceof MapNode)
        {
            Map<String,Node> map = ((MapNode)values).getMappedNodes();
            if (map.isEmpty())
            {
                return sb.append(this.offset(off)).append("{}").append(LINE_BREAK).toString();
            }
            for (Entry<String,Node> entry : map.entrySet())
            {
                String path = entry.getValue().getPath(PATH_SEPARATOR);
                String comment = this.buildComment(container, path, off);
                if (!comment.isEmpty())
                {
                    if (!sb.toString().endsWith(LINE_BREAK + LINE_BREAK)) // if not already one line free
                    {
                        sb.append(LINE_BREAK); // add free line before comment
                    }
                    sb.append(comment);
                }
                sb.append(this.convertValue(container, entry.getValue(), off, inCollection));
            }
            if (!sb.toString().endsWith(LINE_BREAK + LINE_BREAK))
            {
                sb.append(LINE_BREAK);
            }
        }
        else
        {
            throw new IllegalStateException("Error Something went wrong!");
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
        if (this.first)
        {
            this.first = false;
        }
        return comment;
    }

    @Override
    public String getExtension()
    {
        return "yml";
    }
}
