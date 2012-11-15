package de.cubeisland.cubeengine.core.webapi;

/**
 * Common mime types for dynamic content
 */
public class MimeType
{
    public static final MimeType PLAIN = new MimeType("text/plain");
    public static final MimeType HTML = new MimeType("text/html");
    public static final MimeType OCTET_STREAM = new MimeType("application/octet-stream");
    public static final MimeType XML = new MimeType("text/xml");
    public static final MimeType JSON = new MimeType("application/json");
    public static final MimeType CSS = new MimeType("text/css");
    public static final MimeType JAVASCRIPT = new MimeType("text/javascript");
    public static final MimeType GIF = new MimeType("image/gif");
    public static final MimeType JPEG = new MimeType("image/jpeg");
    public static final MimeType PNG = new MimeType("image/png");
    
    private final String typeString;
    private final static String CHARSET = "charset=utf-8";

    public MimeType(String typeString)
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