package de.cubeisland.cubeengine.core.filesystem.gettext;

import java.io.*;

public class MessageCatalogFactory
{
    public MessageCatalogFactory()
    {}

    public MessageCatalog newMessageCatalog(File file)
    {
        if (file.exists())
        {
            try
            {
                if (this.isBinaryCatalog(new FileInputStream(file)))
                {
                    return new MoMessageCatalog(file);
                }
            }
            catch (FileNotFoundException ignored)
            {}
        }
        return new PoMessageCatalog(file);
    }

    private boolean isBinaryCatalog(InputStream is)
    {
        // TODO implement me
        return false;
    }
}
