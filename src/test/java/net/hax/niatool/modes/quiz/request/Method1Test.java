package net.hax.niatool.modes.quiz.request;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class Method1Test {

    @Test
    public void testWrap() throws AnswerNotFoundException {
        int result[];
        String question;
        String answers[] = new String[3];
        question = "pinguino "+System.lineSeparator()+" Imperatore";
        answers[0] = "Polo Nord";
        answers[1] = "A Roma";
        answers[2] = "Antartide";


        MethodToFindAMatch askToGod = new Method1();

        result = askToGod.find(question, answers);
        
        assertThat(result[2] > result[0], is(true));
        assertThat(result[2] > result[1], is(true));
    }

}