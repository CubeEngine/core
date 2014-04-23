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
package de.cubeisland.engine.core.webapi;

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
