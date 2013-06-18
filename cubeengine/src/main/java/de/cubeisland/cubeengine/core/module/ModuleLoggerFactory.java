package de.cubeisland.cubeengine.core.module;

import org.slf4j.Logger;

public interface ModuleLoggerFactory
{

    Logger getLogger(ModuleInfo module);

}
