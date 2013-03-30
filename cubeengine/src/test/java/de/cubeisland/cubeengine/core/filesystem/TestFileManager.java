package de.cubeisland.cubeengine.core.filesystem;

import de.cubeisland.cubeengine.core.Core;

import java.io.File;
import java.io.IOException;

public class TestFileManager extends FileManager
{
    public TestFileManager(Core core) throws IOException
    {
        super(core, new File("test-data"));
    }
}
