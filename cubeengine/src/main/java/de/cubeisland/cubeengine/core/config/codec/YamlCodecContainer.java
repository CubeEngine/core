package de.cubeisland.cubeengine.core.config.codec;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Map;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.MultiConfiguration;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.MapComment;
import de.cubeisland.cubeengine.core.config.annotations.MapComments;
import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.util.StringUtils;

import gnu.trove.map.hash.THashMap;

public class YamlCodecContainer extends MultiCodecContainer<YamlCodecContainer,YamlCodec>
{
    public Map<String, String> comments;

    public YamlCodecContainer(YamlCodec codec)
    {
        super(codec);
        this.comments = new THashMap<String, String>();
    }

    public YamlCodecContainer(YamlCodecContainer superContainer, String parentPath)
    {
        super(superContainer, parentPath);
    }

    @Override
    protected void writeConfigToStream(OutputStream stream, Configuration config) throws IOException
    {
        OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
        if (config.head() != null)
        {
            writer.append("# ").append(StringUtils.implode("\n# ", config.head())).append(codec.LINE_BREAK).append(codec.LINE_BREAK);
        }
        codec.convertMap(writer, this);
        if (config.tail() != null)
        {
            writer.append("# ").append(StringUtils.implode("\n# ", config.tail()));
        }
        writer.flush();
        writer.close();
    }

    /**
     * Adds a comment to be saved
     *
     * @param commentPath the commentPath
     * @param comment the comment
     */
    protected void addComment(String commentPath, String comment)
    {
        if (superContainer == null)
        {
            this.comments.put(commentPath.toLowerCase(), comment);
        }
        else
        {
            superContainer.addComment(this.parentPath + codec.PATH_SEPARATOR + commentPath, comment);
        }
    }

    /**
     * gets the comment for given path
     *
     * @param path the path of the comment
     * @return the comment
     */
    public String getComment(String path)
    {
        return this.comments.get(path);
    }

    @Override
    public <C extends MultiConfiguration> void fillFromFields(C parentConfig, C config, MapNode baseNode)
    {
        Class<C> configClass = (Class<C>) config.getClass();
        this.doMapComments(configClass);
        super.fillFromFields(parentConfig, config, baseNode);
    }

    @Override
    protected void fillFromField(Field field, Configuration config, MapNode baseNode, String path)
    {
        this.doFieldComment(field,path);
        super.fillFromField(field, config, baseNode, path);
    }

    @Override
    public <C extends Configuration> void fillFromFields(C config, MapNode baseNode)
    {
        Class<C> configClass = (Class<C>) config.getClass();
        this.doMapComments(configClass);
        super.fillFromFields(config, baseNode);
    }

    @Override
    protected void fillFromField(Field field, Configuration parentConfig, Configuration config, MapNode baseNode, String path)
    {
        this.doFieldComment(field,path);
        super.fillFromField(field, parentConfig, config, baseNode, path);
    }

    private void doMapComments(Class<? extends Configuration> configClass)
    {
        if (configClass.isAnnotationPresent(MapComments.class))
        {
            MapComment[] mapComments = configClass.getAnnotation(MapComments.class).value();
            for (MapComment comment : mapComments)
            {
                this.addComment(comment.path().replace(".", codec.PATH_SEPARATOR), comment.text());
            }
        }
    }

    private void doFieldComment(Field field, String path)
    {
        if (field.isAnnotationPresent(Comment.class))
        {
            Comment comment = field.getAnnotation(Comment.class);
            this.addComment(path, comment.value());
        }
    }

}
