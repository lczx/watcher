package net.hax.niatool.modes.quiz;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.Image;
import net.hax.niatool.modes.ModeRegistry;
import net.hax.niatool.modes.OperationMode;
import net.hax.niatool.modes.quiz.detector.FeatureDetector;
import net.hax.niatool.modes.quiz.request.AnswerNotFoundException;
import net.hax.niatool.modes.quiz.request.Method1;
import net.hax.niatool.overlay.OverlayViewManager;
import net.hax.niatool.task.ScreenCaptureTask;
import org.jetbrains.annotations.NotNull;

public class QuizHelperMode implements OperationMode {

    @Override
    public ModeRegistry.Info getModeMetadata(@NotNull Resources res) {
        return new ModeRegistry.Info("Quiz Helper", "Come cittare con Live Quiz");
    }

    @Override
    public OverlayViewManager createOverlayManager(@NotNull Context context) {
        return new QuizOverlayManager(context);
    }

    @Override
    public ScreenCaptureTask<?, ?> makeCaptureProcessTask(@NotNull OverlayViewManager overlayManager) {
        return new DummyTask(overlayManager);
    }

    static class DummyTask extends ScreenCaptureTask<Void, int[]> {

        private final QuizOverlayManager overlayManager;

        private DummyTask(OverlayViewManager overlayManager) {
            this.overlayManager = (QuizOverlayManager) overlayManager;
        }

        @Override
        public int[] processCaptureBackground(@NotNull Image image, @NotNull Bitmap capture) {
            FeatureDetector detector = new FeatureDetector(overlayManager.getContext());
            FeatureDetector.Result result = detector.processImage(capture);
            detector.freeResources();

            if(result == null)
                return null;
            try {
                int[] points = new Method1().setNumResults(25).find(result.getQuestion(), result.getAnswers());

                System.out.println(result);
                for (int i : points) {
                    System.out.println(i);

                }
                return points;

            } catch (AnswerNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(int[] s) {
            overlayManager.aggiornaContenuto(s);
        }
    }

}
