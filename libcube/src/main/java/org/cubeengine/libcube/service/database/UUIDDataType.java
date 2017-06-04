/*
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
package org.cubeengine.libcube.service.database;

import static org.jooq.impl.SQLDataType.VARCHAR;

import org.jooq.Binding;
import org.jooq.Configuration;
import org.jooq.Converter;
import org.jooq.DataType;
import org.jooq.EnumType;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.tools.Convert;

import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class UUIDDataType implements DataType<UUID> {

    private static final Pattern TYPE_NAME_PATTERN = Pattern.compile("\\([^\\)]*\\)");

    private final SQLDialect dialect;
    private final DataType<UUID> sqlDataType;
    private final Class<UUID> type;
    private final Binding<?, UUID> binding = VARCHAR(36).asConvertedDataType(new UUIDConverter()).getBinding();
    private final Class<UUID[]> arrayType;
    private final String castTypeName;
    private final String typeName;
    private final boolean nullable;
    private final int length;

    UUIDDataType(boolean nullable) {
        this.dialect = null;

        this.sqlDataType = this;
        this.type = UUID.class;
        this.arrayType = UUID[].class;
        this.typeName = "varchar";
        this.castTypeName = "varchar";

        this.nullable = nullable;
        this.length = 36;
    }

    @Override
    public final DataType<UUID> nullable(boolean n) {
        if (n == nullable) {
            return this;
        }
        return new UUIDDataType(n);
    }

    @Override
    public final boolean nullable() {
        return nullable;
    }

    @Override
    public final DataType<UUID> identity(boolean i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean identity() {
        return false;
    }

    @Override
    public final DataType<UUID> defaultValue(UUID d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final DataType<UUID> defaultValue(Field<UUID> d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Field<UUID> defaultValue() {
        return null;
    }

    @Override
    @Deprecated
    public final DataType<UUID> defaulted(boolean d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean defaulted() {
        return false;
    }

    @Override
    public final DataType<UUID> precision(int p) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final DataType<UUID> precision(int p, int s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int precision() {
        return 0;
    }

    @Override
    public final boolean hasPrecision() {
        return false;
    }

    @Override
    public final DataType<UUID> scale(int s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int scale() {
        return 0;
    }

    @Override
    public final boolean hasScale() {
        return false;
    }

    @Override
    public final DataType<UUID> length(int l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int length() {
        return length;
    }

    @Override
    public final boolean hasLength() {
        return true;
    }

    @Override
    public final DataType<UUID> getSQLDataType() {
        return sqlDataType;
    }

    @Override
    public final DataType<UUID> getDataType(Configuration configuration) {
        return this;
    }

    @Override
    public int getSQLType() {
        return Types.OTHER;
    }

    @Override
    public final Class<UUID> getType() {
        return type;
    }

    @Override
    public final Binding<?, UUID> getBinding() {
        return binding;
    }

    @Override
    public final Converter<?, UUID> getConverter() {
        return binding.converter();
    }

    @Override
    public final Class<UUID[]> getArrayType() {
        return arrayType;
    }

    @Override
    public final String getTypeName() {
        return typeName;
    }

    @Override
    public String getTypeName(Configuration configuration) {
        return getDataType(configuration).getTypeName();
    }

    @Override
    public final String getCastTypeName() {
        return castTypeName + "(" + length + ")";
    }

    @Override
    public String getCastTypeName(Configuration configuration) {
        return getDataType(configuration).getCastTypeName();
    }

    @Override
    public final DataType<UUID[]> getArrayDataType() {
        throw new UnsupportedOperationException();
    }


    @Override
    public final <E extends EnumType> DataType<E> asEnumDataType(Class<E> enumDataType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final <U> DataType<U> asConvertedDataType(Converter<? super UUID, U> converter) {
        return ((DataType<U>) this); // This does not actually set a converter
    }

    @SuppressWarnings("deprecation")
    @Override
    public final <U> DataType<U> asConvertedDataType(Binding<? super UUID, U> newBinding) {
        return ((DataType<U>) this); // This does not actually set a binding
    }

    @Override
    public final SQLDialect getDialect() {
        return dialect;
    }

    @Override
    public UUID convert(Object object) {
        if (object == null) {
            return null;
        } else if (object.getClass() == type) {
            return (UUID) object;
        } else {
            return Convert.convert(object, type);
        }
    }

    @Override
    public final UUID[] convert(Object... objects) {
        return (UUID[]) Convert.convertArray(objects, type);
    }

    @Override
    public final List<UUID> convert(Collection<?> objects) {
        return Convert.convert(objects, type);
    }

    @Override
    public final boolean isNumeric() {
        return Number.class.isAssignableFrom(type) && !isInterval();
    }

    @Override
    public final boolean isString() {
        return false;
    }

    @Override
    public final boolean isDateTime() {
        return false;
    }

    @Override
    public final boolean isTemporal() {
        return false;
    }

    @Override
    public final boolean isInterval() {
        return false;
    }

    @Override
    public final boolean isLob() {
        return false;
    }

    @Override
    public final boolean isBinary() {
        return false;
    }

    @Override
    public final boolean isArray() {
        return false;
    }

    @Override
    public final boolean isUDT() {
        return false;
    }

    @Override
    public String toString() {
        return getCastTypeName() + " (" + type.getName() + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dialect == null) ? 0 : dialect.hashCode());
        result = prime * result + length;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UUIDDataType other = (UUIDDataType) obj;
        if (dialect != other.dialect) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (typeName == null) {
            if (other.typeName != null) {
                return false;
            }
        } else if (!typeName.equals(other.typeName)) {
            return false;
        }
        return true;
    }
}
