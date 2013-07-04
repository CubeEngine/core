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
package de.cubeisland.cubeengine.conomy.account.storage;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseUpdater;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;

import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.IS;

public class AccountStorage extends SingleKeyStorage<Long, AccountModel>
{
    public AccountStorage(final Database database)
    {
        super(database, AccountModel.class, 2);
        this.registerUpdater(new DatabaseUpdater()
        {
            @Override
            public void update(Database database) throws SQLException
            {
                QueryBuilder builder = database.getQueryBuilder();
                database.execute(builder.alterTable(tableName).drop("currencyName").end().end());
                database.execute(builder.alterTable(tableName).change("hidden", "mask",AttrType.TINYINT).end().end());
            }
        },1);
        this.initialize();
    }

    @Override
    protected void prepareStatements() throws SQLException
    {
        super.prepareStatements();
        QueryBuilder builder = this.database.getQueryBuilder();
        // Get User-Account
        this.database.storeStatement(modelClass, "getUserAccount",
                                     builder.select().cols(allFields).from(this.tableName).
                                         where().field("user_id").isEqual().value().end().end());
        // Get Bank-Account
        this.database.storeStatement(modelClass, "getBankAccount",
                                     builder.select().cols(allFields).from(this.tableName).
                                         where().field("name").isEqual().value().end().end());
        // Get Bank-Accounts
        this.database.storeStatement(modelClass, "getBankAccounts", builder.select().cols("name").from(this.tableName).
                                            where().not().field("name").is(IS).value(null).
                                            and().field("mask").beginFunction("&").value(1).endFunction().isEqual().value(1).isEqual().value().end().end());
        // Get Top-Accounts VALUES: showhidden, user, bank, limit, offset
        this.database.storeStatement(modelClass, "getTop",
                                     builder.select().cols(allFields).from(this.tableName).
                                        where().field("mask").beginFunction("&").value(1).endFunction().isEqual().value(1).isEqual().value(). // Hidden bit is set?
                                          and().beginSub().field("name").is(IS).value(null).isEqual().value().
                                         or().field("user_id").is(IS).value(null).isEqual().value().endSub().
                                     orderBy("value").desc().limit().offset().end().end());
        // Sets the balance of all accounts
        this.database.storeStatement(modelClass, "setAll",
                                     builder.update(this.tableName).set("value").
                                         where().field("name").is(IS).value(null).isEqual().value().
                                            or().field("user_id").is(IS).value(null).isEqual().value()
                                         .end().end());
        // Changes the balance of all accounts
        this.database.storeStatement(modelClass, "transactAll",
                             builder.update(this.tableName).set("value").beginFunction("+").field("value").endFunction().
                                where().field("name").is(IS).value(null).isEqual().value().
                                   or().field("user_id").is(IS).value(null).isEqual().value()
                                    .end().end());
        // Scales the balance of all accounts
        this.database.storeStatement(modelClass, "scaleAll",
                         builder.update(this.tableName).set("value").beginFunction("*").field("value").endFunction().
                             where().field("name").is(IS).value(null).isEqual().value().
                                    or().field("user_id").is(IS).value(null).isEqual().value()
                                .end().end());
        // Sets all accounts to a hidden-state
        this.database.storeStatement(modelClass, "setAllHidden",
                                     builder.update(this.tableName).set("mask").beginFunction("|").field("mask").endFunction().
                                         where().field("name").is(IS).value(null).isEqual().value().
                                                or().field("user_id").is(IS).value(null).isEqual().value()
                                            .end().end());
        this.database.storeStatement(modelClass, "unsetAllHidden",
                                     builder.update(this.tableName).set("mask").beginFunction("& ~").field("mask").endFunction().
                                         where().field("name").is(IS).value(null).isEqual().value().
                                                or().field("user_id").is(IS).value(null).isEqual().value()
                                            .end().end());
    }

    public Collection<AccountModel> getTopAccounts(boolean user, boolean bank, int fromRank, int toRank, boolean showHidden)
    {
        try
        {
            ResultSet resultSet;
            resultSet = this.database.preparedQuery(modelClass, "getTop", showHidden, user, bank, toRank + 1  - fromRank, fromRank - 1);
            LinkedList<AccountModel> list = new LinkedList<AccountModel>();
            while (resultSet.next())
            {
                AccountModel loadedModel = this.modelClass.newInstance();
                for (Field field : this.fieldNames.keySet())
                {
                    field.set(loadedModel, resultSet.getObject(this.fieldNames.get(field)));
                }
                list.add(loadedModel);
            }
            return list;
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while reading from Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
        }
    }

    public void transactAll(boolean user, boolean bank, long amount)
    {
        try
        {
            this.database.preparedUpdate(modelClass, "transactAll", amount, user, bank);
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while updating database", ex);
        }
    }

    public void setAll(boolean user, boolean bank, long amount)
    {
        try
        {
            this.database.preparedUpdate(modelClass, "setAll", amount, user, bank);
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while updating database", ex);
        }
    }

    public void scaleAll(boolean user, boolean bank, float factor)
    {
        try
        {
            this.database.preparedUpdate(modelClass, "scaleAll", factor, user, bank);
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while updating database", ex);
        }
    }

    public AccountModel getBankAccount(String name)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getBankAccount", name);
            if (resulsSet.next())
            {
                AccountModel loadedModel = this.modelClass.newInstance();
                for (Field field : this.fieldNames.keySet())
                {
                    field.set(loadedModel, resulsSet.getObject(this.fieldNames.get(field)));
                }
                return loadedModel;
            }
            return null;
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while reading from Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
        }
    }

    public AccountModel getUserAccount(long userID)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getUserAccount", userID);
            if (resulsSet.next())
            {
                AccountModel loadedModel = this.modelClass.newInstance();
                for (Field field : this.fieldNames.keySet())
                {
                    field.set(loadedModel, resulsSet.getObject(this.fieldNames.get(field)));
                }
                return loadedModel;
            }
            return null;
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while reading from Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
        }
    }

    public void setAllHidden(boolean user, boolean bank, boolean set)
    {
        try
        {
            if (set)
            {
                this.database.preparedUpdate(modelClass, "setAllHidden", 1, user, bank);
            }
            else
            {
                this.database.preparedUpdate(modelClass, "unsetAllHidden", 1, user, bank);
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while updating database", ex);
        }
    }

    public Set<String> getBankAccounts(boolean hidden)
    {
        try
        {
            ResultSet resultSet = this.database.preparedQuery(modelClass, "getBankAccounts", hidden);
            Set<String> result = new HashSet<String>();
            while (resultSet.next())
            {
                result.add(resultSet.getString("name"));
            }
            return result;
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while reading from Database", ex);
        }
    }
}
