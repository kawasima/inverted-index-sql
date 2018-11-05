package crawler;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class WikipediaCrawlerFactory implements CrawlController.WebCrawlerFactory {
    private final DataSource ds;

    public WikipediaCrawlerFactory() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:file:./wikipedia");
        config.setUsername("sa");
        ds = new HikariDataSource(config);

        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .load();
        flyway.migrate();
    }

    @Override
    public WebCrawler newInstance() throws Exception {
        return new WikipediaCrawler(ds);
    }
}
