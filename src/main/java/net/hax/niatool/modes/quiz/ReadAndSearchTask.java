package net.hax.niatool.modes.quiz;

import android.graphics.Bitmap;
import android.media.Image;
import net.hax.niatool.modes.quiz.detector.FeatureDetector;
import net.hax.niatool.modes.quiz.request.AnswerNotFoundException;
import net.hax.niatool.modes.quiz.request.Method1;
import net.hax.niatool.overlay.OverlayViewManager;
import net.hax.niatool.task.ScreenCaptureTask;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadAndSearchTask extends ScreenCaptureTask<Void, ReadAndSearchTask.ResultData> {

    private static final Logger LOG = LoggerFactory.getLogger(ReadAndSearchTask.class);

    private final QuizOverlayManager overlayManager;

    ReadAndSearchTask(OverlayViewManager overlayManager) {
        this.overlayManager = (QuizOverlayManager) overlayManager;
    }

    @Override
    public ResultData processCaptureBackground(@NotNull Image image, @NotNull Bitmap capture) {
        FeatureDetector detector = new FeatureDetector(overlayManager.getContext());
        FeatureDetector.Result ocrResult = detector.processImage(capture);
        detector.freeResources();

        if (ocrResult == null) {
            LOG.warn("Character recognition failed, shot was probably taken on the wrong screen");
            return null;
        }

        try {
            int[] points = new Method1().setNumResults(25).find(ocrResult.getQuestion(), ocrResult.getAnswers());
            return new ResultData(points, ocrResult.getAnswerYCoordinates());
        } catch (AnswerNotFoundException e) {
            LOG.warn("No search results found");
            return null;
        }
    }


    @Override
    protected void onPreExecute() {
        overlayManager.onTaskStart();
    }

    @Override
    protected void onPostExecute(ResultData resultData) {
        if (resultData != null)
            overlayManager.onResult(resultData.values, resultData.yCoords);
        else
            overlayManager.onResult(null, null);
    }

    static class ResultData {
        private int[] values;
        private int[] yCoords;

        public ResultData(int[] values, int[] yCoords) {
            this.values = values;
            this.yCoords = yCoords;
        }
    }

}
