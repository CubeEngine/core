package de.cubeisland.engine.module.confirm;

import de.cubeisland.engine.modularity.asm.marker.Service;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.contract.NotNull;

@Service
@Version(1)
public interface ConfirmManager
{
    void registerConfirmation(ConfirmResult confirmResult, Module module, CommandSender sender);

    int countPendingConfirmations(@NotNull CommandSender sender);

    ConfirmResult getLastPendingConfirmation(CommandSender sender);
}
