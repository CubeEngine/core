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
package de.cubeisland.cubeengine.vote;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.Account_old;

import com.vexsoftware.votifier.model.VotifierEvent;

public class Vote extends Module implements Listener
{
    private Conomy conomy;

    @Override
    public void onEnable()
    {
        this.getCore().getEventManager().registerListener(this, this);
    }

    @EventHandler
    private void onVote(VotifierEvent event)
    {
        final com.vexsoftware.votifier.model.Vote vote = event.getVote();
        final User user = this.getCore().getUserManager().getUser(vote.getUsername());
        final Account_old account = this.conomy.getAccountsManager().getAccount(user);
    }
}
