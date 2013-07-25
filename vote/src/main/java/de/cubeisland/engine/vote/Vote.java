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
package de.cubeisland.engine.vote;

import java.sql.Timestamp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.service.Economy;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.vote.storage.VoteManager;
import de.cubeisland.engine.vote.storage.VoteModel;

import com.vexsoftware.votifier.model.VotifierEvent;

public class Vote extends Module implements Listener
{
    private VoteConfiguration config;

    private VoteManager manager;

    @Override
    public void onEnable()
    {
        this.config = Configuration.load(VoteConfiguration.class, this);
        this.getCore().getEventManager().registerListener(this, this);
        this.manager = new VoteManager(this);
    }

    @EventHandler
    private void onVote(VotifierEvent event)
    {
        final com.vexsoftware.votifier.model.Vote vote = event.getVote();
        if (this.getCore().getUserManager().getUser(vote.getUsername(), false) != null)
        {
            User user = this.getCore().getUserManager().getUser(vote.getUsername());
            Economy economy = this.getCore().getServiceManager().getServiceProvider(Economy.class);
            VoteModel voteModel = this.manager.get(user.key);
            if (voteModel == null)
            {
                voteModel = new VoteModel(user);
                this.manager.store(voteModel);
            }
            else
            {
                if (System.currentTimeMillis() - voteModel.lastvote.getTime() > this.config.votebonustime.toMillis())
                {
                    voteModel.voteamount = 1;
                }
                else
                {
                    voteModel.voteamount++;
                }
                voteModel.lastvote = new Timestamp(System.currentTimeMillis());
                this.manager.update(voteModel);
            }
            economy.createPlayerAccount(vote.getUsername());
            double money = this.config.votereward * (Math.pow(1+1.5/voteModel.voteamount, voteModel.voteamount-1));
            economy.deposit(vote.getUsername(), money);
            String moneyFormat = economy.format(money);
            this.getCore().getUserManager().broadcastMessage(this.config.votebroadcast.
                replace("{PLAYER}", vote.getUsername()).
                replace("{MONEY}", moneyFormat).
                replace("{AMOUNT}", String.valueOf(voteModel.voteamount)));
            user.sendMessage(ChatFormat.parseFormats(this.config.votemessage.
                replace("{PLAYER}", vote.getUsername()).
                replace("{MONEY}", moneyFormat).
                replace("{AMOUNT}", String.valueOf(voteModel.voteamount))));
        }
    }
}
