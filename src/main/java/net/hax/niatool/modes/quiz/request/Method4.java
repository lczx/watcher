package net.hax.niatool.modes.quiz.request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Cerca la domanda e nella prima pagina di google incentrata sul sito di wikipedia conta quante volte compaiono le parole di ogni risposta
 */
public class Method4 extends MethodToFindAMatch {

    private static final Logger LOG = LoggerFactory.getLogger(Method4.class);

    @Override
    public int[] compute(String question, String[] answers) {
        int occorrenze[] = {0, 0, 0};
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("q", question);
            parameters.put("as_sitesearch", "it.wikipedia.org");

            Document doc = Jsoup.connect(makeUrl(parameters)).get();
            Elements risultati = doc.getElementsByClass("g");

            UselessWordRemover screma = new UselessWordRemover();
            String wordsA0[] = screma.lookIn(answers[0]).split(" ");
            String wordsA1[] = screma.lookIn(answers[1]).split(" ");
            String wordsA2[] = screma.lookIn(answers[2]).split(" ");

            for (Element risultato : risultati) {
                final String risToLowerCase = risultato.text().toLowerCase();

                for (String wordA0 : wordsA0) {
                    if (risToLowerCase.contains(wordA0)) {
                        occorrenze[0] += risToLowerCase.split(wordA0).length - 1;
                    }
                }

                for (String wordA1 : wordsA1) {
                    if (risToLowerCase.contains(wordA1)) {
                        occorrenze[1] += risToLowerCase.split(wordA1).length - 1;
                    }
                }

                for (String wordA2 : wordsA2) {
                    if (risToLowerCase.contains(wordA2)) {
                        occorrenze[2] += risToLowerCase.split(wordA2).length - 1;
                    }
                }
            }
        } catch (IOException ex) {
            LOG.error("I/O exception while fetching document", ex);
        }
        return occorrenze;
    }

}
