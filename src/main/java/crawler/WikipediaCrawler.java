package crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jooq.*;
import org.jooq.impl.DSL;
import tokenizer.KuromojiTokenizer;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.jooq.impl.DSL.*;

public class WikipediaCrawler extends WebCrawler {
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz))$");
    private final KuromojiTokenizer tokenizer;
    private final DSLContext create;
    private final StoreQuery insDocument;
    private final StoreQuery insToken;
    private final StoreQuery insInvertedIndex;
    private final ResultQuery<Record1<Object>> selToken;

    public WikipediaCrawler(DataSource ds) throws SQLException {
        tokenizer = new KuromojiTokenizer();
        create = DSL.using(ds.getConnection());
        insDocument = (StoreQuery) create
                .insertInto(
                        table("documents"),
                        field("id"),
                        field("url"),
                        field("title"),
                        field("body"))
                .values(param(), param(), param(), param())
                .keepStatement(true);
        selToken = create.select(field("id"))
                .from(table("tokens"))
                .where(
                        field("word").eq(param())
                ).keepStatement(true);
        insToken = (StoreQuery) create
                .insertInto(table("tokens"),
                        field("id"),
                        field("word")
                )
                .values(param(), param())
                .keepStatement(true);
        insInvertedIndex = (StoreQuery) create
                .insertInto(table("inverted_indexes"),
                        field("token_id"),
                        field("document_id"),
                        field("position")
                )
                .values(param(), param(), param())
                .keepStatement(true);


    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.startsWith("https://ja.wikipedia.org/wiki/");
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String title = htmlParseData.getTitle();

            create.transaction(new TransactionalRunnable() {
                @Override
                public void run(Configuration configuration) throws Throwable {
                    UUID docUUID = UUID.randomUUID();
                    byte[] docId = ByteBuffer.allocate(16)
                            .putLong(docUUID.getLeastSignificantBits())
                            .putLong(docUUID.getMostSignificantBits())
                            .array();
                    insDocument
                            .bind(1, docId)
                            .bind(2, url)
                            .bind(3, title)
                            .bind(4, text)
                            .execute();
                    Set<String> tokens = tokenizer.tokenize(text);
                    for (String token : tokens) {

                        byte[] tokenId = (byte[]) selToken.bind(1, token).fetchAny(field("id"));
                        if (tokenId == null) {
                            UUID tokenUUID = UUID.randomUUID();
                            tokenId = ByteBuffer.allocate(16)
                                    .putLong(tokenUUID.getLeastSignificantBits())
                                    .putLong(tokenUUID.getMostSignificantBits())
                                    .array();
                            insToken.bind(1, tokenId)
                                    .bind(2, token)
                                    .execute();
                        }

                        insInvertedIndex
                                .bind(1, tokenId)
                                .bind(2, docId)
                                .bind(3, 1l)
                                .execute();
                    }
                }
            });
        }
    }

    public void insert(DataSource ds) throws SQLException {
    }
}
