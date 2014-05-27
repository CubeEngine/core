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
package de.cubeisland.engine.core.storage;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.storage.database.Table;
import de.cubeisland.engine.core.util.Version;
import gnu.trove.map.hash.THashMap;
import org.jooq.DSLContext;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class Registry extends Table<RegistryModel>
{
    private final THashMap<String, THashMap<String, String>> data = new THashMap<>();

    public void merge(Module module, String key, String value)
    {
        this.loadForModule(module);
        this.dsl.insertInto(this, this.KEY, this.MODULE, this.VALUE).values(key, module.getId(), value)
                .onDuplicateKeyUpdate().set(VALUE, value).execute();
        this.data.get(module.getId()).put(key, value);
    }

    public String delete(Module module, String key)
    {
        this.loadForModule(module);
        this.dsl.delete(this).where(MODULE.eq(module.getId()), KEY.eq(key)).execute();
        return this.data.get(module.getId()).remove(key);
    }

    public void loadForModule(Module module)
    {
        if (this.data.get(module.getId()) == null)
        {
            THashMap<String, String> map = this.data.get(module.getId());
            this.data.put(module.getId(), map);
            for (RegistryModel registryModel : this.dsl.selectFrom(this).where(MODULE.eq(module.getId())).fetch())
            {
                map.put(registryModel.getKey(), registryModel.getValue());
            }
        }
    }

    public String getValue(String key, Module module)
    {
        this.loadForModule(module);
        return this.data.get(module.getId()).get(key);
    }

    public void clear(Module module)
    {
        this.dsl.delete(this).where(MODULE.eq(module.getId())).execute();
    }

    public static Registry TABLE_REGISTRY;
    private DSLContext dsl;

    public Registry(String prefix)
    {
        super(prefix + "registry", new Version(1));
        this.setPrimaryKey(KEY, MODULE);
        this.addFields(KEY, MODULE, VALUE);
        TABLE_REGISTRY = this;
    }

    public void setDsl(DSLContext dsl)
    {
        this.dsl = dsl;
    }

    public final TableField<RegistryModel, String> KEY = createField("key", SQLDataType.VARCHAR.length(16).nullable(false), this);
    public final TableField<RegistryModel, String> MODULE = createField("module", SQLDataType.VARCHAR.length(16).nullable(false), this);
    public final TableField<RegistryModel, String> VALUE = createField("value", SQLDataType.VARCHAR.length(256).nullable(false), this);

    @Override
    public Class<RegistryModel> getRecordType() {
        return RegistryModel.class;
    }
}
