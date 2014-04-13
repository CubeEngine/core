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
package de.cubeisland.engine.signmarket;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.Reloadable;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.signmarket.storage.TableSignBlock;
import de.cubeisland.engine.signmarket.storage.TableSignItem;

public class Signmarket extends Module implements Reloadable
{
    private MarketSignFactory marketSignFactory;
    private SignMarketConfig config;
    private EditModeListener editModeListener;
    private MarketSignPerm perms;
    private SignMarketCommands smCmds;

    @Override
    public void onEnable()
    {
        Database db = this.getCore().getDB();
        db.registerTable(TableSignItem.class); // Init Item-table first!!!
        db.registerTable(TableSignBlock.class);
        this.marketSignFactory = new MarketSignFactory(this);
        this.marketSignFactory.loadInAllSigns();
        this.editModeListener = new EditModeListener(this);
        this.getCore().getEventManager().registerListener(this, new MarketSignListener(this));
        smCmds = new SignMarketCommands(this);
        this.getCore().getCommandManager().registerCommand(smCmds);
        this.perms = new MarketSignPerm(this, smCmds);
        this.config = this.loadConfig(SignMarketConfig.class);
    }

    @Override
    public void reload()
    {
        Database db = this.getCore().getDB();
        db.registerTable(TableSignItem.class); // Init Item-table first!!!
        db.registerTable(TableSignBlock.class);
        this.config = this.loadConfig(SignMarketConfig.class);
        this.marketSignFactory = new MarketSignFactory(this);
        this.marketSignFactory.loadInAllSigns();
        this.editModeListener = new EditModeListener(this);
        this.getCore().getEventManager().registerListener(this, new MarketSignListener(this));
        this.perms = new MarketSignPerm(this, smCmds);
    }

    public MarketSignFactory getMarketSignFactory()
    {
        return this.marketSignFactory;
    }

    public SignMarketConfig getConfig()
    {
        return this.config;
    }

    public EditModeListener getEditModeListener()
    {
        return this.editModeListener;
    }

    public MarketSignPerm perms()
    {
        return this.perms;
    }
}
