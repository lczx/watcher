package net.hax.niatool.modes.quiz.request;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;

class SaveHtmlInFile {
    private Context context;
    private final String filename = "html.html";

    //catella logs/data+ora+minuti+secondi/

    public SaveHtmlInFile(Context context) {
        this.context = context;
    }

    void save(String html) {

        FileOutputStream outputStream;
        

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(html.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
