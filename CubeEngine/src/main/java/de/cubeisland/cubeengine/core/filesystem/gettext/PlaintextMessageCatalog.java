package de.cubeisland.cubeengine.core.filesystem.gettext;

import gnu.trove.map.hash.THashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

public class PlaintextMessageCatalog implements MessageCatalog
{
    private final File file;

    public PlaintextMessageCatalog(File file)
    {
        this.file = file;
    }

    public Map<String, String> read(InputStream inputStream) throws IOException
    {
        Map<String, String> messages = new THashMap<String, String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        int spaceOffset;

        String currentId = null;

        while ((line = reader.readLine()) != null)
        {
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#')
            {
                continue;
            }
            spaceOffset = line.indexOf(' ');
            if (spaceOffset == -1)
            {
                continue;
            }
            String type = line.substring(0, spaceOffset);
            String text = line.substring(spaceOffset).trim();
            if (text.length() > 2 && text.charAt(text.length() - 1) == text.charAt(0))
            {
                text = text.substring(1, text.length() - 1);
            }

            if (currentId == null)
            {
                if (type.equalsIgnoreCase("msgid"))
                {
                    currentId = text;
                }
                else
                {
                    continue;
                }
            }
            else
            {
                messages.put(currentId, text);
                currentId = null;
            }
        }

        return messages;
    }

    public Map<String, String> read() throws IOException
    {
        return this.read(new FileInputStream(this.file));
    }

    public void write(Map<String, String> messages) throws IOException
    {

    }

    public void write(OutputStream outputStream, Map<String, String> messages) throws IOException
    {

    }
}
