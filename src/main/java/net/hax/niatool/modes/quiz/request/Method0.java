package net.hax.niatool.modes.quiz.request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Cerca la domanda insieme a una risposta e confronta il numero di risultati ottenuti
 */
public class Method0 extends MethodToFindAMatch {

    private static final Logger LOG = LoggerFactory.getLogger(Method0.class);

    @Override
    public int[] compute(String question, String[] answers){
        int resultStats[] = new int[3];
        for (int i = 0; i < 3; i++) {
            resultStats[i] = countResultStatsFor(question, answers[i]);
        }

        return resultStats;
    }

    private int countResultStatsFor(String question, String answer) {
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("q", question + " " + answer);
            Document doc = Jsoup.connect(makeUrl(parameters)).get();

            Element resultStats = doc.getElementById("resultStats");

            return Integer.parseInt(resultStats.ownText().replaceAll("[^0-9]", ""));

        } catch (IOException ex) {
            LOG.error("I/O exception while fetching document", ex);
        }
        return 0;
    }

}
