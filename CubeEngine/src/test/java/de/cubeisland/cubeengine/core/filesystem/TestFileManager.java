package de.cubeisland.cubeengine.core.filesystem;

import java.io.File;
import java.io.IOException;

public class TestFileManager extends FileManager
{
    public TestFileManager() throws IOException
    {
        super(new File("test-data"));
    }
}
