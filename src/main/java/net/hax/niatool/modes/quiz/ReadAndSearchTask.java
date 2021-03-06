package net.hax.niatool.modes.quiz;

import android.graphics.Bitmap;
import android.media.Image;
import net.hax.niatool.BuildConfig;
import net.hax.niatool.modes.quiz.detector.FeatureDetector;
import net.hax.niatool.modes.quiz.request.AnswerNotFoundException;
import net.hax.niatool.modes.quiz.request.Method1;
import net.hax.niatool.modes.quiz.request.MethodToFindAMatch;
import net.hax.niatool.overlay.OverlayViewManager;
import net.hax.niatool.task.ScreenCaptureTask;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ReadAndSearchTask extends ScreenCaptureTask<ReadAndSearchTask.ProgressInfo, ReadAndSearchTask.ResultData>
        implements FeatureDetector.StatusListener, MethodToFindAMatch.StatusListener {

    private static final Logger LOG = LoggerFactory.getLogger(ReadAndSearchTask.class);

    private final QuizOverlayManager overlayManager;

    ReadAndSearchTask(OverlayViewManager overlayManager) {
        super(overlayManager);
        this.overlayManager = (QuizOverlayManager) overlayManager;
    }

    @Override
    public ResultData processCaptureBackground(@NotNull Image image, @NotNull Bitmap capture) {
        ReportGenerator reportGen = BuildConfig.DEBUG ? new ReportGenerator(overlayManager.getContext()) : null;

        publishProgress(new ProgressInfo(0, "Initializing"));
        FeatureDetector detector = overlayManager.getFeatureDetector();
        detector.setStatusListener(this);
        FeatureDetector.Result ocrResult = detector.processImage(capture, reportGen);

        if (ocrResult == null) {
            LOG.warn("Character recognition failed, shot was probably taken on the wrong screen");
            return null;
        }
        LOG.debug("OCR Result: {}", ocrResult);

        ResultData resultData;
        try {
            Method1 m = new Method1(reportGen);
            m.setStatusListener(this);
            int[] points = m.setNumResults(25).find(ocrResult.getQuestion(), ocrResult.getAnswers());

            resultData = new ResultData(points, ocrResult.getAnswerYCoordinates());
            LOG.debug("Search result: {}", resultData);
        } catch (AnswerNotFoundException e) {
            LOG.warn("No search results found");
            resultData = null;
        }

        if (BuildConfig.DEBUG) reportGen.saveReport();
        return resultData;
    }


    @Override
    protected void onPreExecute() {
        overlayManager.onTaskStart();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ResultData resultData) {
        if (resultData != null)
            overlayManager.onResult(resultData.values, resultData.yCoords);
        else
            overlayManager.onResult(null, null);
    }

    @Override
    protected void onProgressUpdate(ProgressInfo... values) {
        overlayManager.onProgressUpdate(values[0].percentage, values[0].description);
    }

    @Override
    public void onFeatureDetectorUpdate(FeatureDetector.Step step) {
        switch (step) {
            case MEASURING_COMPONENTS:
                publishProgress(new ProgressInfo(10, "Measuring"));
                break;
            case CROPPING_BITMAPS:
                publishProgress(new ProgressInfo(20, "Cropping"));
                break;
            case OCR_QUESTION:
                publishProgress(new ProgressInfo(30, "OCR Q"));
                break;
            case OCR_ANS1:
                publishProgress(new ProgressInfo(40, "OCR A1"));
                break;
            case OCR_ANS2:
                publishProgress(new ProgressInfo(50, "OCR A2"));
                break;
            case OCR_ANS3:
                publishProgress(new ProgressInfo(60, "OCR A3"));
                break;
        }
    }

    @Override
    public void onSearchUpdate(MethodToFindAMatch.Step step) {
        switch (step) {
            case FETCH_DOCUMENT:
                publishProgress(new ProgressInfo(70, "Fetching"));
                break;
            case PROCESS_DOCUMENT:
                publishProgress(new ProgressInfo(90, "Parsing"));
                break;
        }
    }

    static class ProgressInfo {
        private int percentage;
        private String description;

        public ProgressInfo(int percentage, String description) {
            this.percentage = percentage;
            this.description = description;
        }
    }

    static class ResultData {
        private int[] values;
        private int[] yCoords;

        public ResultData(int[] values, int[] yCoords) {
            this.values = values;
            this.yCoords = yCoords;
        }

        @Override
        public String toString() {
            return "ResultData{" +
                    "values=" + Arrays.toString(values) +
                    ", yCoords=" + Arrays.toString(yCoords) +
                    '}';
        }
    }

}
