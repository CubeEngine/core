package de.cubeisland.cubeengine.core.filesystem.gettext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public interface GettextCatalog
{
    Map<String, String> read() throws IOException;
    Map<String, String> read(InputStream inputStream) throws IOException;
    void write(Map<String, String> messages) throws IOException;
    void write(OutputStream outputStream, Map<String, String> messages) throws IOException;
}
