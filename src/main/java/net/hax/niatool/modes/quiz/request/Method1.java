package net.hax.niatool.modes.quiz.request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Cerca la domanda e nella prima pagina di google trovata conta quante volte compaiono le parole di ogni risposta
 * Ordina in base alla maggiore
 */
public class Method1 extends MethodToFindAMatch {

    private static final Logger LOG = LoggerFactory.getLogger(Method1.class);

    private int numResults = 10;

    public Method1 setNumResults(int numResults) {
        this.numResults = numResults;
        return this;
    }

    @Override
    public int[] compute(String question, String[] answers){
        int occorrenze[] = {0, 0, 0};
        try {
            if (statusListener != null) statusListener.onSearchUpdate(Step.FETCH_DOCUMENT);

            Map<String, String> parameters = new HashMap<>();
            parameters.put("q", question);
            parameters.put("num", Integer.toString(numResults));
            Document doc = Jsoup.connect(makeUrl(parameters)).get();
            if (statusListener != null) statusListener.onSearchUpdate(Step.PROCESS_DOCUMENT);
            Elements risultati = doc.getElementsByClass("g");
            Elements risultatoInEvidenza = doc.getElementsByClass("rl_container");

            UselessWordRemover screma = new UselessWordRemover();
            for(int i=0 ; i<3; i++){
                answers[i]=screma.lookIn(answers[i]);
            }
            ArrayList<String> wordsA0 = new ArrayList<>(Arrays.asList(answers[0].split(" ")));
            ArrayList<String> wordsA1 = new ArrayList<>(Arrays.asList(answers[1].split(" ")));
            ArrayList<String> wordsA2 = new ArrayList<>(Arrays.asList(answers[2].split(" ")));
            wordsA0.add(answers[0]);
            wordsA1.add(answers[1]);
            wordsA2.add(answers[2]);

            for (Element risultato : risultatoInEvidenza) {
                searchInOneResultFor(occorrenze, wordsA0, wordsA1, wordsA2, risultato);
            }

            for(int i =0; i<3; i++){
                occorrenze[i]=occorrenze[i]*100;
            }

            for (Element risultato : risultati) {
                searchInOneResultFor(occorrenze, wordsA0, wordsA1, wordsA2, risultato);
            }
        } catch (IOException ex) {
            LOG.error("I/O exception while fetching document", ex);
        }
        return occorrenze;
    }

    private void searchInOneResultFor(int[] occorrenze, ArrayList<String> wordsA0, ArrayList<String> wordsA1, ArrayList<String> wordsA2, Element risultato) {
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

}
