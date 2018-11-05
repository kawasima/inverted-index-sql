package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.jooq.impl.DSL.*;

public class V1__CreateInvertedIndex extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        createDocuments(connection);
        createTokens(connection);
        createInvertedIndex(connection);
    }

    private void createDocuments(Connection connection) throws SQLException {
        DSLContext create = DSL.using(connection);
        String sql = create.createTable(table("documents"))
                .column(field("id", SQLDataType.BINARY(16).nullable(false)))
                .column(field("title", SQLDataType.VARCHAR(255).nullable(false)))
                .column(field("url", SQLDataType.VARCHAR(255).nullable(false)))
                .column(field("body", SQLDataType.CLOB))
                .constraints(
                        constraint().primaryKey(field("id"))
                )
                .getSQL();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void createTokens(Connection connection) throws SQLException {
        DSLContext create = DSL.using(connection);
        String sql = create.createTable(table("tokens"))
                .column(field("id", SQLDataType.BINARY(16).nullable(false)))
                .column(field("word", SQLDataType.VARCHAR(255).nullable(false)))
                .constraints(
                        constraint().primaryKey(field("id")),
                        constraint().unique(field("word"))
                )
                .getSQL();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void createInvertedIndex(Connection connection) throws SQLException {
        DSLContext create = DSL.using(connection);
        String sql = create.createTable(table("inverted_indexes"))
                .column(field("token_id", SQLDataType.BINARY(16).nullable(false)))
                .column(field("document_id", SQLDataType.BINARY(16).nullable(false)))
                .column(field("position", SQLDataType.BIGINT.nullable(false)))
                .constraints(
                        constraint().primaryKey(field("token_id"), field("document_id")),
                        constraint().foreignKey(field("token_id")).references(table("tokens"), field("id")),
                        constraint().foreignKey(field("document_id")).references(table("documents"), field("id"))
                )
                .getSQL();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
