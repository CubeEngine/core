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
package de.cubeisland.engine.module.paginate;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.module.core.command.CommandManager;
import de.cubeisland.engine.module.core.sponge.EventManager;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.user.UserManager;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerQuitEvent;

@ServiceImpl(PaginationManager.class)
@Version(1)
public class SpongePaginationManager implements PaginationManager
{
    public static final String HEADER =          "--------- page {integer}/{integer} ---------";
    public static final String FOOTER =          "- /prev - page {integer}/{integer} - /next -";
    public static final String FIRST_FOOTER =    "--------- page {integer}/{integer} - /next -";
    public static final String LAST_FOOTER =     "- /prev - page {integer}/{integer} ---------";
    public static final String ONE_PAGE_FOOTER = "--------- page {integer}/{integer} ---------";
    public static final int LINES_PER_PAGE = 5;

    private Map<CommandSender, PaginatedResult> userCommandMap = new HashMap<>();
    private CoreModule core;

    @Inject
    public SpongePaginationManager(EventManager em, CommandManager cm, CoreModule core)
    {
        this.core = core;
        cm.addCommands(cm, core, new PaginationCommands(this));
        em.registerListener(core, this);
    }

    @Subscribe
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        userCommandMap.remove(core.getModularity().start(UserManager.class).getExactUser(event.getPlayer().getUniqueId()));
    }

    @Override
    public void registerResult(CommandSender sender, PaginatedResult result)
    {
        userCommandMap.put(sender, result);
    }

    @Override
    public PaginatedResult getResult(CommandSender sender)
    {
        return userCommandMap.get(sender);
    }

    @Override
    public boolean hasResult(CommandSender sender)
    {
        return userCommandMap.containsKey(sender);
    }
}
