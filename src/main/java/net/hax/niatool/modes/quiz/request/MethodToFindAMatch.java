package net.hax.niatool.modes.quiz.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class MethodToFindAMatch {

    private static final Logger LOG = LoggerFactory.getLogger(MethodToFindAMatch.class);
    private static final String GOOGLE_SEARCH_URL = "http://www.google.com/search?";

    protected StatusListener statusListener;

    public void setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    /**
     * @return Torna la percentuale di probabilità (max=100) della correttezza delle risposta nello stesso ordine delle risposte date
     */
    public int[] find(String question, String[] answers) throws AnswerNotFoundException {
        question = question.toLowerCase();

        if (!question.contains(" non ")) {
            return compute(question, answers);
        }

        int[] result = compute(question.replace(" non ", " "), answers);
        for (int i = 0; i < 3; i++) {
            result[i] = 100 - result[i];
        }
        return normalizeOccurrences(result);
    }

    public abstract int[] compute(String question, String[] answers) throws AnswerNotFoundException;

    /**
     * @param parameters Inserire una mappa con key pari al parametro da ricercare su google ( "q" per il testo da cercare,  "btnI" per mi sento fortunato)
     */
    protected String makeUrl(Map<String, String> parameters) {
        try {
            StringBuilder url = new StringBuilder(GOOGLE_SEARCH_URL);
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                url
                        .append(parameter.getKey())
                        .append('=')
                        .append(URLEncoder.encode(parameter.getValue(), StandardCharsets.UTF_8.name()))
                        .append('&');
            }
            url.deleteCharAt(url.length() - 1);
            return url.toString();
        } catch (UnsupportedEncodingException ex) {
            LOG.error("Encoding exception", ex);
        }
        return "";
    }

    protected int[] normalizeOccurrences(int[] occorrenze) throws AnswerNotFoundException {
        int sum = 0;
        for (int occorrenza : occorrenze) {
            sum = sum + occorrenza;
        }
        if (sum == 0) {
            throw new AnswerNotFoundException();
        }

        for (int i = 0; i < 3; i++) {
            occorrenze[i] = occorrenze[i] * 100 / sum;
        }

        return occorrenze;
    }

    public enum Step {
        FETCH_DOCUMENT,
        PROCESS_DOCUMENT
    }

    public interface StatusListener {
        void onSearchUpdate(Step step);
    }

}
