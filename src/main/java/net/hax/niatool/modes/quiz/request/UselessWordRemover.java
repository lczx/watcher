package net.hax.niatool.modes.quiz.request;

public class UselessWordRemover {
    private final String articoli[] = {"il", "lo", "la", "i", "gli", "le", "l'", "un", "uno", "una", "un'", "del", "dello", "della", "delle", "degli"};
    private final String preposizioni[] = {"di", "a", "da", "in", "con", "su", "per", "tra", "fra"};

    protected String lookIn(String frase) {
        frase = frase.toLowerCase();

        frase = eliminate(frase, articoli);
        frase = eliminate(frase, preposizioni);

        return frase;
    }

    private String eliminate(String frase, String badWords[]) {
        for (String badWord : badWords) {
            frase = frase.replaceAll(" " + badWord + " ", " ");
            if (frase.startsWith(badWord + " ")) {
                frase = frase.substring(badWord.length() + 1);
            }
        }
        return frase;
    }
}
