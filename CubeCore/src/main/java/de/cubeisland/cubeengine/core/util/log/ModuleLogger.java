package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.module.ModuleInfo;
import java.io.File;
import java.util.Locale;
import java.util.logging.Level;

/**
 *
 * @author Phillip Schichtel
 */
public class ModuleLogger extends CubeLogger
{
    public ModuleLogger(Core core, ModuleInfo info)
    {
        super(info.getName());
        try
        {
            this.addHandler(new FileHandler(Level.ALL, new File(core.getFileManager().getLogDir(), info.getName().toLowerCase(Locale.ENGLISH) + ".log").toString()));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
