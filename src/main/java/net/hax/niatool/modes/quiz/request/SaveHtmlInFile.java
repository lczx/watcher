package net.hax.niatool.modes.quiz.request;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaveHtmlInFile {

    private static final String FILENAME_HTML = "html.html";
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("MMdd_hhmmss", Locale.ROOT);

    private final File logDir;

    public SaveHtmlInFile(Context context) {
        logDir = new File(context.getExternalFilesDir(null), "logs/" + TIMESTAMP_FORMAT.format(new Date()));
        logDir.mkdirs();
    }

    void save(String html) {
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(logDir, FILENAME_HTML));
            outputStream.write(html.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
