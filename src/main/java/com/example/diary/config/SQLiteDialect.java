package com.example.diary.config;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;

import java.sql.Types;

public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();
        // В Hibernate 7 capacity имеет тип long, поэтому используем -1L
        registerColumnType(Types.BIT, -1L, "integer");
        registerColumnType(Types.TINYINT, -1L, "tinyint");
        registerColumnType(Types.SMALLINT, -1L, "smallint");
        registerColumnType(Types.INTEGER, -1L, "integer");
        registerColumnType(Types.BIGINT, -1L, "bigint");
        registerColumnType(Types.FLOAT, -1L, "float");
        registerColumnType(Types.REAL, -1L, "real");
        registerColumnType(Types.DOUBLE, -1L, "double");
        registerColumnType(Types.NUMERIC, -1L, "numeric");
        registerColumnType(Types.DECIMAL, -1L, "decimal");
        registerColumnType(Types.CHAR, -1L, "char");
        registerColumnType(Types.VARCHAR, -1L, "varchar");
        registerColumnType(Types.LONGVARCHAR, -1L, "longvarchar");
        registerColumnType(Types.DATE, -1L, "date");
        registerColumnType(Types.TIME, -1L, "time");
        registerColumnType(Types.TIMESTAMP, -1L, "datetime");
        registerColumnType(Types.BINARY, -1L, "blob");
        registerColumnType(Types.VARBINARY, -1L, "blob");
        registerColumnType(Types.LONGVARBINARY, -1L, "blob");
        registerColumnType(Types.BLOB, -1L, "blob");
        registerColumnType(Types.CLOB, -1L, "clob");
        registerColumnType(Types.BOOLEAN, -1L, "integer");
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new SQLiteIdentityColumnSupport();
    }

    @Override
    public boolean hasAlterTable() {
        return false;
    }

    @Override
    public boolean dropConstraints() {
        return false;
    }

    @Override
    public String getDropForeignKeyString() {
        return "";
    }

    @Override
    public String getAddForeignKeyConstraintString(
            String constraintName, String[] foreignKey, String referencedTable,
            String[] primaryKey, boolean referencesPrimaryKey) {
        return "";
    }

    @Override
    public String getAddPrimaryKeyConstraintString(String constraintName) {
        return "";
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    @Override
    public boolean supportsCascadeDelete() {
        return false;
    }

    private static class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {
        @Override
        public boolean supportsIdentityColumns() {
            return true;
        }

        @Override
        public String getIdentitySelectString(String table, String column, int type) {
            return "select last_insert_rowid()";
        }

        @Override
        public String getIdentityColumnString(int type) {
            return "integer";
        }

        @Override
        public boolean hasDataTypeInIdentityColumn() {
            return false;
        }
    }
}