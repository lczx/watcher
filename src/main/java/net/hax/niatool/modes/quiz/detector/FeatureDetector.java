package net.hax.niatool.modes.quiz.detector;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.RawRes;
import com.googlecode.tesseract.android.TessBaseAPI;
import net.hax.niatool.R;
import net.hax.niatool.modes.quiz.ReportGenerator;

import java.io.*;
import java.util.Arrays;


public class FeatureDetector {

    private static final String TESSERACT_CACHE_FOLDER = "tesseract";
    private static final String TESSERACT_TRAINED_DATA_DIR = "tessdata";
    private static final String TESSERACT_TRAINED_DATA_EXT = ".traineddata";

    private static final int WHITE_THRESHOLD = 240;
    private static final int HEADER_HEIGHT_DP = 145;
    private static final int BORDER_QUESTION_DP = 30;
    private static final int BORDER_ANSWER_DP = 70;

    private final TessBaseAPI tesseract;
    private StatusListener statusListener;

    public FeatureDetector(Context context) {
        //https://solidgeargroup.com/ocr-on-android
        tesseract = new TessBaseAPI();
        initializeEngine(context, "ita", R.raw.tessdata_ita);
    }

    public void setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    public Result processImage(Bitmap bitmap, ReportGenerator reportGen) {
        if (reportGen != null) reportGen.putImage("img_capture.png", bitmap);

        if (statusListener != null) statusListener.onFeatureDetectorUpdate(Step.MEASURING_COMPONENTS);

        int[] verticalMiddleArray = new int[bitmap.getHeight()];
        bitmap.getPixels(verticalMiddleArray, 0, 1, bitmap.getWidth() * 3 / 4, 0, 1, bitmap.getHeight());

        int bottomBlueBox = 0;
        int answerBox = 0;
        int infraAnswerBox = 0;
        boolean changed1 = false;
        boolean changed2 = false;
        boolean correct = false;
        for (int i = verticalMiddleArray.length - 1; i >= bitmap.getHeight() / 2; i--) {
            int red = (verticalMiddleArray[i] & 0xFF0000) >> 16;
            if (red > WHITE_THRESHOLD && !changed1 && !changed2) {
                bottomBlueBox = bitmap.getHeight() - i - 1;
                changed1 = true;
            } else if (red < WHITE_THRESHOLD && changed1 && !changed2) {
                answerBox = bitmap.getHeight() - bottomBlueBox - i - 1;
                changed2 = true;
            } else if (red > WHITE_THRESHOLD && changed1 && changed2) {
                infraAnswerBox = bitmap.getHeight() - answerBox - bottomBlueBox - i - 1;
                changed1 = false;
            } else if (red < WHITE_THRESHOLD && !changed1 && changed2) {
                correct = true;
                break;
            }
        }

        if (correct) {
            if (statusListener != null) statusListener.onFeatureDetectorUpdate(Step.CROPPING_BITMAPS);

            int paddedAnswerBox = infraAnswerBox + answerBox;
            int ans3Y = bitmap.getHeight() - bottomBlueBox - answerBox;
            int ans2Y = ans3Y - paddedAnswerBox;
            int ans1Y = ans3Y - 2 * paddedAnswerBox;

            int headerHeight = (int) (HEADER_HEIGHT_DP * bitmap.getDensity() / 160f);
            int questionBorder = (int) (BORDER_QUESTION_DP * bitmap.getDensity() / 160f);
            int questionHeight = ans3Y - 2 * paddedAnswerBox - headerHeight - 5;
            Bitmap bmpQuestion = Bitmap.createBitmap(bitmap,
                    questionBorder / 2, headerHeight,
                    bitmap.getWidth() - questionBorder, questionHeight);

            int ansBorder = (int) (BORDER_ANSWER_DP * bitmap.getDensity() / 160f);
            int ansWidth = bitmap.getWidth() - ansBorder;

            Bitmap bmpAnswer1 = Bitmap.createBitmap(bitmap, ansBorder / 2, ans1Y, ansWidth, answerBox);
            Bitmap bmpAnswer2 = Bitmap.createBitmap(bitmap, ansBorder / 2, ans2Y, ansWidth, answerBox);
            Bitmap bmpAnswer3 = Bitmap.createBitmap(bitmap, ansBorder / 2, ans3Y, ansWidth, answerBox);

            if (statusListener != null) statusListener.onFeatureDetectorUpdate(Step.OCR_QUESTION);
            String question = getOCRResult(bmpQuestion);
            if (statusListener != null) statusListener.onFeatureDetectorUpdate(Step.OCR_ANS1);
            String answer1 = getOCRResult(bmpAnswer1);
            if (statusListener != null) statusListener.onFeatureDetectorUpdate(Step.OCR_ANS2);
            String answer2 = getOCRResult(bmpAnswer2);
            if (statusListener != null) statusListener.onFeatureDetectorUpdate(Step.OCR_ANS3);
            String answer3 = getOCRResult(bmpAnswer3);

            if (reportGen != null) {
                reportGen.putImage("img_question.png", bmpQuestion);
                reportGen.putImage("img_answer1.png", bmpAnswer1);
                reportGen.putImage("img_answer2.png", bmpAnswer2);
                reportGen.putImage("img_answer3.png", bmpAnswer3);
                reportGen.putText("ocr_results.txt", String.format(
                        "Q:  %s\r\nA1: %s\r\nA2: %s\r\nA3: %s\r\n", question, answer1, answer2, answer3));
            }

            int ansBoxHalf = answerBox / 2;
            return new Result(question, new String[]{answer1, answer2, answer3},
                    new int[]{ans1Y + ansBoxHalf, ans2Y + ansBoxHalf, ans3Y + ansBoxHalf});
        } else {
            return null;
        }
    }

    public void freeResources() {
        tesseract.end();
    }

    private void initializeEngine(Context context, String language, @RawRes int langResData) {
        File tCacheDir = new File(context.getCacheDir(), TESSERACT_CACHE_FOLDER);
        File tTrainedDataDir = new File(tCacheDir, TESSERACT_TRAINED_DATA_DIR);
        File tDataFile = new File(tTrainedDataDir, language + TESSERACT_TRAINED_DATA_EXT);
        if (!tDataFile.isFile()) {
            tTrainedDataDir.mkdirs();
            dumpStreamToFile(context.getResources().openRawResource(langResData), tDataFile);
        }
        tesseract.init(tCacheDir.getAbsolutePath(), language);
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

    public enum Step {
        MEASURING_COMPONENTS,
        CROPPING_BITMAPS,
        OCR_QUESTION,
        OCR_ANS1,
        OCR_ANS2,
        OCR_ANS3
    }

    public interface StatusListener {
        void onFeatureDetectorUpdate(Step step);
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
            return question;
        }

        public String[] getAnswers() {
            return answers;
        }

        public int[] getAnswerYCoordinates() {
            return ansPosY;
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
