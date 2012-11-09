package de.cubeisland.cubeengine.core.webapi.server;

/**
 * Common mime types for dynamic content
 */
public enum MimeType
{
    PLAIN("text/plain"),
    HTML("text/html"),
    OCTET_STREAM("application/octet-stream"),
    XML("text/xml"),
    JSON("application/json"),
    CSS("text/css"),
    JAVASCRIPT("text/javascript"),
    GIF("image/gif"),
    JPEG("image/jpeg"),
    PNG("image/png");
    private final String typeString;
    private final static String CHARSET = "charset=utf-8";

    MimeType(String typeString)
    {
        this.typeString = typeString;
    }

    public String getTypeString()
    {
        return this.typeString;
    }

    @Override
    public String toString()
    {
        return this.typeString + "; " + CHARSET;
    }
}