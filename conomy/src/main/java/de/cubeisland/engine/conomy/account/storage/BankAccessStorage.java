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
package de.cubeisland.engine.conomy.account.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import de.cubeisland.engine.core.storage.SingleKeyStorage;
import de.cubeisland.engine.core.storage.StorageException;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.user.User;

public class BankAccessStorage extends SingleKeyStorage<Long, BankAccessModel>
{
    private static final int REVISION = 1;

    public BankAccessStorage(Database database)
    {
        super(database, BankAccessModel.class, REVISION);
        this.initialize();
    }

    @Override
    protected void prepareStatements() throws SQLException
    {
        super.prepareStatements();
        //TODO DATABASE
        /*
        QueryBuilder builder = this.database.getQueryBuilder();
        this.database.storeStatement(this.modelClass, "getBankAccess", builder.select(allFields).from(this.tableName)
                                                                              .where().field("accountId").isEqual()
                                                                              .value().end().end());
        this.database.storeStatement(this.modelClass, "getUserAccess", builder.select("accountId").from(this.tableName)
                                                                              .where().field("userId").isEqual()
                                                                              .value().end().end());
*/
    }

    public Set<BankAccessModel> getBankAccess(AccountModel model)
    {
        try
        {
            ResultSet resultSet = this.database.preparedQuery(this.modelClass, "getBankAccess", model.getId());
            Set<BankAccessModel> result = new HashSet<BankAccessModel>();
            while (resultSet.next())
            {
                long id = resultSet.getLong("id");
                long userId = resultSet.getLong("userId");
                long accountId = resultSet.getLong("accountId");
                byte accountType = resultSet.getByte("accessLevel");
                result.add(new BankAccessModel(id, userId, accountId, accountType, model.getName()));
            }
            return result;
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not get BankAccess-Data from Database!", ex, this.database.getStoredStatement(this.modelClass, "getBankAccess"));
        }
    }

    public Set<Long> getBankAccounts(User user)
    {
        try
        {
            ResultSet resultSet = this.database.preparedQuery(this.modelClass, "getUserAccess", user.getId());
            Set<Long> result = new HashSet<Long>();
            while (resultSet.next())
            {
                result.add(resultSet.getLong("accountId"));
            }
            return result;
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not get BankAccess-Data from Database!", ex, this.database.getStoredStatement(this.modelClass, "getUserAccess"));
        }
    }
}
