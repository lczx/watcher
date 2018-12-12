package net.hax.niatool.modes.quiz.request;

import android.content.Context;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class SaveHtmlInFile {
    private Context context;
    private final String filenameHTML = "html.html";
    private final String path;

    public SaveHtmlInFile(Context context) {
        this.context = context;
        this.path="logs/"+timeToString()+"/";
    }

    void save(String html) {

        FileOutputStream outputStream;


        try {
            outputStream = context.openFileOutput(path+filenameHTML, Context.MODE_PRIVATE);
            outputStream.write(html.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String timeToString(){
        SimpleDateFormat sdf1 = new SimpleDateFormat("MMdd_hhmmss", Locale.ITALIAN);
        return sdf1.format(new Date());
    }
}
