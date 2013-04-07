/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.vaultcompat;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.conomy.Conomy;

import net.milkbowl.vault.economy.Economy;

public class Vaultcompat extends Module
{
    @Override
    public void onEnable()
    {
        BukkitCore core = this.getCore();
        ServicesManager servicesManager = core.getServer().getServicesManager();

        final ModuleManager mm = core.getModuleManager();

        Module module = mm.getModule("conomy");
        if (module != null && module instanceof Conomy)
        {
            servicesManager.register(Economy.class, new VaultConomyService(this, (Conomy)module), core, ServicePriority.Highest);
        }

        module = mm.getModule("roles");
        if (module != null && module instanceof Conomy)
        {
            servicesManager.register(Economy.class, new VaultConomyService(this, (Conomy)module), core, ServicePriority.Highest);

            module = mm.getModule("chat");
            if (module != null && module instanceof Conomy)
            {
                servicesManager.register(Economy.class, new VaultConomyService(this, (Conomy)module), core, ServicePriority.Highest);
            }
        }
    }

    @Override
    public BukkitCore getCore()
    {
        return (BukkitCore)super.getCore();
    }
}
