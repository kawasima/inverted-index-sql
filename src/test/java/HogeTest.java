import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class HogeTest {
    @Test
    public void test() throws IOException {
        Map<String, String> options = new HashMap<>();
        TokenizerFactory factory = TokenizerFactory.forName("japanese", options);
        Tokenizer tokenizer = factory.create();
        CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
        PartOfSpeechAttribute partOfSpeech = tokenizer.addAttribute(PartOfSpeechAttribute.class);
        tokenizer.reset();

        StringReader input = new StringReader("今日東京都から京都に行きました。にわにはにはにわとりがいました。");
        tokenizer.setReader(input);
        tokenizer.reset();
        while(tokenizer.incrementToken()) {
            System.out.println(term.toString() + "\t" +  partOfSpeech.getPartOfSpeech());
        }
        tokenizer.close();

        input = new StringReader("すもももももももものうち");
        tokenizer.setReader(input);
        tokenizer.reset();
        while(tokenizer.incrementToken()) {
            System.out.println(term.toString() + "\t" +  partOfSpeech.getPartOfSpeech());
        }
        tokenizer.close();

    }
}
