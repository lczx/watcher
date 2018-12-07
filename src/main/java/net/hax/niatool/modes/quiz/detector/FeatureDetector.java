package net.hax.niatool.modes.quiz.detector;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.RawRes;
import com.googlecode.tesseract.android.TessBaseAPI;
import net.hax.niatool.R;

import java.io.*;
import java.util.Arrays;


public class FeatureDetector {

    private static final String TESSERACT_CACHE_FOLDER = "/tesseract/";
    private static final String TESSERACT_DATA_EXT = ".traineddata";

    private static final int WHITE_THRESHOLD = 240;
    private static final int HEADER_HEIGHT_DP = 145;
    private static final int BORDER_QUESTION_DP = 30;
    private static final int BORDER_ANSWER_DP = 70;

    private final TessBaseAPI tesseract;

    public FeatureDetector(Context context) {
        //https://solidgeargroup.com/ocr-on-android
        tesseract = new TessBaseAPI();
        initializeEngine(context, "ita", R.raw.tessdata_ita);
    }

    public Result processImage(Bitmap bitmap) {
        int borderQ = (int) (BORDER_QUESTION_DP * bitmap.getDensity() / 160f);
        int width = bitmap.getWidth() - borderQ;
        int height = bitmap.getHeight();

        int[] verticalMiddleArray = new int[height];
        bitmap.getPixels(verticalMiddleArray, 0, 1, width * 3 / 4, 0, 1, height);

        int bottomBlueBox = 0;
        int answerBox = 0;
        int infraAnswerBox = 0;
        boolean changed1 = false;
        boolean changed2 = false;
        boolean correct = false;
        int red;
        for (int i = verticalMiddleArray.length - 1; i >= height / 2; i--) {
            red = Integer.parseInt(Integer.toHexString(verticalMiddleArray[i]).substring(2, 4), 16);
            if (red > WHITE_THRESHOLD && !changed1 && !changed2) {
                bottomBlueBox = height - i - 1;
                changed1 = true;
            } else if (red < WHITE_THRESHOLD && changed1 && !changed2) {
                answerBox = height - bottomBlueBox - i - 1;
                changed2 = true;
            } else if (red > WHITE_THRESHOLD && changed1 && changed2) {
                infraAnswerBox = height - answerBox - bottomBlueBox - i - 1;
                changed1 = false;
            } else if (red < WHITE_THRESHOLD && !changed1 && changed2) {
                correct = true;
                break;
            }
        }

        if (correct) {
            int headerHeight = (int) (HEADER_HEIGHT_DP * bitmap.getDensity() / 160f);

            int paddedAnswerBox = infraAnswerBox + answerBox;

            int ans3Y = height - bottomBlueBox - answerBox;
            int ans2Y = ans3Y - paddedAnswerBox;
            int ans1Y = ans3Y - 2 * paddedAnswerBox;

            int questionHeight = ans3Y - 2 * paddedAnswerBox - headerHeight - 5;
            Bitmap bmpQuestion = Bitmap.createBitmap(bitmap,
                    borderQ / 2, headerHeight,
                    bitmap.getWidth() - borderQ, questionHeight);

            int ansBorder = (int) (BORDER_ANSWER_DP * bitmap.getDensity() / 160f);
            int ansWidth = bitmap.getWidth() - ansBorder;

            Bitmap bmpAnswer1 = Bitmap.createBitmap(bitmap, ansBorder / 2, ans1Y, ansWidth, answerBox);
            Bitmap bmpAnswer2 = Bitmap.createBitmap(bitmap, ansBorder / 2, ans2Y, ansWidth, answerBox);
            Bitmap bmpAnswer3 = Bitmap.createBitmap(bitmap, ansBorder / 2, ans3Y, ansWidth, answerBox);

            /*
            saveBitmap(bmpQuestion, "q");
            saveBitmap(bmpAnswer1, "a1");
            saveBitmap(bmpAnswer2, "a2");
            saveBitmap(bmpAnswer3, "a3");

            Bitmap bmpQ = Bitmap.createBitmap(bitmap, width * 3 / 4, 0, 1, height);
            saveBitmap(bmpQ, "q");
            */

            int ansBoxHalf = answerBox / 2;
            return new Result(
                    getOCRResult(bmpQuestion),
                    new String[]{getOCRResult(bmpAnswer1), getOCRResult(bmpAnswer2), getOCRResult(bmpAnswer3)},
                    new int[]{ans1Y + ansBoxHalf, ans2Y + ansBoxHalf, ans3Y + ansBoxHalf});
        } else {
            return null;
        }
    }

    /*void saveBitmap(Bitmap bmp, String suffix) {
        String filename = String.format("/sdcard/bla-%s.png", suffix);

        try (FileOutputStream out = new FileOutputStream(filename)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public void freeResources() {
        tesseract.end();
    }

    private void initializeEngine(Context context, String language, @RawRes int langResData) {
        String tCacheDir = context.getExternalCacheDir() + TESSERACT_CACHE_FOLDER;
        File tDataFile = new File(tCacheDir, language + TESSERACT_DATA_EXT);
        if (!tDataFile.isFile()) {
            new File(tCacheDir).mkdirs();
            dumpStreamToFile(context.getResources().openRawResource(langResData), tDataFile);
        }
        tesseract.init(tCacheDir, language);
    }

    private String getOCRResult(Bitmap image) {
        tesseract.setImage(image);
        return tesseract.getUTF8Text();
    }

    private void dumpStreamToFile(InputStream in, File outputFile) {
        try {
            OutputStream out = new FileOutputStream(outputFile);

            int read;
            byte[] buffer = new byte[8192];

            while ((read = in.read(buffer)) != -1)
                out.write(buffer, 0, read);

            out.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Result {
        private final String question;
        private final String[] answers;
        private final int[] ansPosY;

        private Result(String question, String[] answers, int[] ansPosY) {
            this.question = question;
            this.answers = answers;
            this.ansPosY = ansPosY;
        }

        public String getQuestion() {
            return this.question;
        }

        public String[] getAnswers() {
            return this.answers;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "question='" + question + '\'' +
                    ", answers=" + Arrays.toString(answers) +
                    ", ansPosY=" + Arrays.toString(ansPosY) +
                    '}';
        }
    }

}
