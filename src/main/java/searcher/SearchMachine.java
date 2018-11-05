package searcher;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.*;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SearchMachine {
    private final DataSource ds;

    public SearchMachine(DataSource ds) {
        this.ds = ds;
    }
    public void searchByLike(String keyword) throws SQLException {
        try(Connection connection = ds.getConnection()) {
            DSLContext create = DSL.using(connection);
            ResultQuery<Record2<Object, Object>> query = create
                    .select(field("documents.id"),
                            field("documents.title"))
                    .from(table("documents"))
                    .where(field("documents.body").like("%" + keyword + "%"))
                    .keepStatement(true);
            long t1 = System.nanoTime();
            Result<Record2<Object, Object>> result = query.fetch();
            long t2 = System.nanoTime();
            System.out.println(result);
            System.out.println(((t2-t1)/1_000_000) + "msec");
        }
    }

    public void search(String keyword) throws SQLException {
        try(Connection connection = ds.getConnection()) {
            DSLContext create = DSL.using(connection);
            ResultQuery<Record2<Object, Object>> query = create
                    .select(field("documents.id"),
                            field("documents.title"))
                    .from(table("documents")
                            .join(table("inverted_indexes"))
                            .on(field("documents.id").eq(field("inverted_indexes.document_id")))
                            .join(table("tokens"))
                            .on(field("inverted_indexes.token_id").eq(field("tokens.id")))
                    )
                    .where(
                            field("tokens.word").eq(param())
                    )
                    .keepStatement(true);

            query.bind(1, keyword);
            long t1 = System.nanoTime();
            Result<Record2<Object, Object>> result = query.fetch();
            long t2 = System.nanoTime();
            System.out.println(result);
            System.out.println(((t2-t1)/1_000_000) + "msec");
        }
    }
}
