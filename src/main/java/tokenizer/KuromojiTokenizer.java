package tokenizer;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.TokenizerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KuromojiTokenizer {
    private final Tokenizer tokenizer;
    private final CharTermAttribute term;
    private final PartOfSpeechAttribute partOfSpeech;

    public KuromojiTokenizer() {
        Map<String, String> options = new HashMap<>();
        TokenizerFactory factory = TokenizerFactory.forName("japanese", options);
        tokenizer = factory.create();
        term = tokenizer.addAttribute(CharTermAttribute.class);
        partOfSpeech = tokenizer.addAttribute(PartOfSpeechAttribute.class);
    }

    public Set<String> tokenize(String sentence) throws IOException  {
        tokenizer.setReader(new StringReader(sentence));
        tokenizer.reset();
        Set<String> tokens = new HashSet<>();
        while(tokenizer.incrementToken()) {
            String parts = partOfSpeech.getPartOfSpeech();
            if (!parts.startsWith("助詞") && !parts.startsWith("助動詞"))
            tokens.add(term.toString());
        }
        tokenizer.close();
        return tokens;
    }
}
