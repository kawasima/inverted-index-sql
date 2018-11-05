package searcher;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

public class SearchMachineTest {
    @Test
    public void test() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:file:./wikipedia");
        config.setUsername("sa");
        DataSource ds = new HikariDataSource(config);

        SearchMachine machine = new SearchMachine(ds);
        machine.search("日本");
        machine.searchByLike("日本");
    }
}
