package de.cubeisland.cubeengine.core.config.codec;

public interface CommentableCodec<Container extends CodecContainer>
{
    /**
     * Builds a the comment for given path
     *
     * @param path the path
     * @param off the current offset
     * @return the comment
     */
    public String buildComment(Container container, String path, int off);
}
