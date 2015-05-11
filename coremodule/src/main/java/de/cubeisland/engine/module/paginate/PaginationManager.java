package de.cubeisland.engine.module.paginate;

import de.cubeisland.engine.modularity.asm.marker.Service;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.module.core.command.CommandSender;

@Service
@Version(1)
public interface PaginationManager
{
    void registerResult(CommandSender sender, PaginatedResult result);

    PaginatedResult getResult(CommandSender sender);

    boolean hasResult(CommandSender sender);
}
