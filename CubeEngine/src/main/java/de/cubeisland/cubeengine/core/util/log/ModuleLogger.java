package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.module.ModuleInfo;
import java.io.File;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This logger is used to log module messages.
 */
public class ModuleLogger extends CubeLogger
{
    private final String prefix;

    public ModuleLogger(Core core, ModuleInfo info)
    {
        super(info.getName(), core.getCoreLogger());
        this.prefix = "[" + info.getName() + "] ";
        try
        {
            this.addHandler(new CubeFileHandler(Level.ALL, new File(core.getFileManager().getLogDir(), info.getName().toLowerCase(Locale.ENGLISH)).toString()));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void log(LogRecord record)
    {
        record.setMessage(this.prefix + record.getMessage());
        super.log(record);
    }
}