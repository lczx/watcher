package net.hax.niatool.modes.quiz;

import android.content.Context;
import android.graphics.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ReportGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ReportGenerator.class);
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("MMdd_hhmmss", Locale.ROOT);

    private final File logDir;
    private final List<Saveable> toSave = new LinkedList<>();

    public ReportGenerator(Context context) {
        logDir = new File(context.getExternalFilesDir(null), "logs/" + TIMESTAMP_FORMAT.format(new Date()));
        logDir.mkdirs();
    }

    public void putImage(String name, Bitmap image) {
        toSave.add(new ImageFile(name, image));
    }

    public void putText(String name, String text) {
        toSave.add(new TextFile(name, text));
    }

    void saveReport() {
        for (Saveable s : toSave) {
            try {
                s.save(logDir);
            } catch (IOException e) {
                LOG.error("Cannot save report file: \"" + s.getFileName() + '"', e);
            }
        }
    }

    static abstract class Saveable {
        protected final String fileName;

        private Saveable(String fileName) {
            this.fileName = fileName;
        }

        private String getFileName() {
            return fileName;
        }

        public abstract void save(File directory) throws IOException;
    }

    static class TextFile extends Saveable {
        private final String text;

        private TextFile(String fileName, String text) {
            super(fileName);
            this.text = text;
        }

        @Override
        public void save(File directory) throws IOException {
            FileOutputStream out = new FileOutputStream(new File(directory, fileName));
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.close();
        }
    }

    static class ImageFile extends Saveable {
        private final Bitmap image;

        private ImageFile(String fileName, Bitmap image) {
            super(fileName);
            this.image = image;
        }

        @Override
        public void save(File directory) throws IOException {
            FileOutputStream out = new FileOutputStream(new File(directory, fileName));
            image.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        }
    }

}
