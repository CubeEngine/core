package de.cubeisland.cubeengine.core.filesystem.gettext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class MoMessageCatalog implements MessageCatalog
{
    @Override
    public Map<String, String> read() throws IOException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, String> read(InputStream inputStream) throws IOException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void write(Map<String, String> messages) throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void write(OutputStream outputStream, Map<String, String> messages) throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
