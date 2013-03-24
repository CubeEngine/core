package de.cubeisland.cubeengine.core.filesystem.gettext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
                    return new BinaryMessageCatalog(file);
                }
            }
            catch (FileNotFoundException ignored)
            {}
        }
        return new PlaintextMessageCatalog(file);
    }

    private boolean isBinaryCatalog(InputStream is)
    {
        // TODO implement me
        return false;
    }
}
