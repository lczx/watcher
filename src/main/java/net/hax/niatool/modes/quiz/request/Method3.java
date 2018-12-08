package net.hax.niatool.modes.quiz.request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Ti senti preciso? Questo metodo cerca nel primo risultato di google per il sito di wikipedia e conta quante volte compaiono le parole delle varie risposte
 */
public class Method3 extends MethodToFindAMatch {

    private static final Logger LOG = LoggerFactory.getLogger(Method3.class);

    @Override
    public int[] compute(String question, String[] answers) throws AnswerNotFoundException {
        return normalizeOccurrences(countWordsInWikipediaPage(question, answers));
    }

    private int[] countWordsInWikipediaPage(String question, String[] answers) throws AnswerNotFoundException {
        int occorrenze[] = new int[3];
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("q", "site:it.wikipedia.org " + question);
            parameters.put("btnI", "Mi+sento+fortunato");
            Document doc = Jsoup.connect(makeUrl(parameters)).get();
            String bodyText = doc.getElementById("content").text().toLowerCase();

            for (int i = 0; i < 3; i++) {
                occorrenze[i] = searchFor(bodyText, answers[i]);
            }

        } catch (IOException ex) {
            LOG.error("I/O exception while fetching document", ex);
        } catch (NullPointerException e) { //Significa che non ha trovato un sito di wikipedia dalla ricerca
            throw new AnswerNotFoundException();
        }

        return occorrenze;
    }

    private int searchFor(String body, String answer) {
        int occorrenze = 0;
        UselessWordRemover screma = new UselessWordRemover();
        String words[] = screma.lookIn(answer).split(" ");
        for (String word : words) {
            occorrenze += body.split(word).length - 1;
        }
        return occorrenze;
    }

}
