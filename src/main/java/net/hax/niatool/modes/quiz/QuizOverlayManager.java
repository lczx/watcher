package net.hax.niatool.modes.quiz;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.support.v4.view.GravityCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import net.hax.niatool.OverlayServiceUtil;
import net.hax.niatool.R;
import net.hax.niatool.modes.quiz.detector.FeatureDetector;
import net.hax.niatool.overlay.ControlPanelOverlay2;
import net.hax.niatool.overlay.OverlayViewManager;
import org.jetbrains.annotations.NotNull;

public class QuizOverlayManager extends OverlayViewManager {

    private static final int PROGRESS_MARGIN_BOTTOM_DP = 40;
    private static final int RESULT_MARGIN_RIGHT_DP = 60;

    private final ProgressOverlay progressOverlay;
    private ResultDisplayOverlay[] resultOverlays = new ResultDisplayOverlay[3];

    private OcrInitializer ocrInitializer;
    private FeatureDetector featureDetector;

    public QuizOverlayManager(@NotNull Context context) {
        super(context);
        progressOverlay = new ProgressOverlay(context);
    }

    public FeatureDetector getFeatureDetector() {
        return featureDetector;
    }

    @Override
    public void configureControlOverlay(@NotNull ControlPanelOverlay2 controlOverlay) {
        controlOverlay.addScene(new CaptureScene());
        controlOverlay.switchScene(CaptureScene.class);
    }

    void onTaskStart() {
        clearResults();
        progressOverlay.setProgress(0, null);
        progressOverlay.getViewport().setVisibility(View.VISIBLE);
    }

    void onProgressUpdate(int percentage, String description) {
        progressOverlay.setProgress(percentage, description);
    }

    void onResult(int[] values, int[] yCoords) {
        progressOverlay.getViewport().setVisibility(View.INVISIBLE);

        if (values == null) {
            Toast.makeText(getContext(), "No results have been found!", Toast.LENGTH_LONG).show();
            return;
        }

        for (int i = 0; i < resultOverlays.length; ++i) {
            if (resultOverlays[i] == null) {
                resultOverlays[i] = new ResultDisplayOverlay(getContext());
            } else {
                getWindowManager().removeView(resultOverlays[i].getViewport());
            }

            resultOverlays[i].setText(String.valueOf(values[i]));
            getWindowManager().addView(resultOverlays[i].getViewport(),
                    makeResultLayoutParams(yCoords[i], resultOverlays[i]));
        }
    }

    @Override
    public void onProjectionStart() {
        super.onProjectionStart();
        getWindowManager().addView(progressOverlay.getViewport(), makeProgressLayoutParams());
        progressOverlay.getViewport().setVisibility(View.INVISIBLE);
        ocrInitializer = (OcrInitializer) new OcrInitializer(this).execute();
    }

    @Override
    public void onProjectionStop() {
        getWindowManager().removeView(progressOverlay.getViewport());
        clearResults();
        if (ocrInitializer.getStatus() != AsyncTask.Status.FINISHED)
            ocrInitializer.cancel(true);
        else
            ocrInitializer.onCancelled();

        super.onProjectionStop();
    }

    private void clearResults() {
        for (int i = 0; i < resultOverlays.length; ++i) {
            if (resultOverlays[i] != null) {
                getWindowManager().removeView(resultOverlays[i].getViewport());
                resultOverlays[i] = null;
            }
        }
    }

    private WindowManager.LayoutParams makeProgressLayoutParams() {
        int marginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                PROGRESS_MARGIN_BOTTOM_DP, getContext().getResources().getDisplayMetrics());

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, -1, marginBottom,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, // Type
                OverlayViewManager.getLAYOUT_FLAGS_DEFAULT(), // Flags
                PixelFormat.TRANSLUCENT); // Format
        p.gravity = Gravity.BOTTOM;
        return p;
    }

    private WindowManager.LayoutParams makeResultLayoutParams(int y, ResultDisplayOverlay resultOverlay) {
        int marginRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                RESULT_MARGIN_RIGHT_DP, getContext().getResources().getDisplayMetrics());

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        resultOverlay.getViewport().measure(widthMeasureSpec, heightMeasureSpec);

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                marginRight, y - resultOverlay.getViewport().getMeasuredHeight() / 2,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, // Type
                OverlayViewManager.getLAYOUT_FLAGS_DEFAULT(), // Flags
                PixelFormat.TRANSLUCENT); // Format
        p.gravity = Gravity.TOP | GravityCompat.END;
        return p;
    }

    private class CaptureScene extends ControlPanelOverlay2.Scene {
        public CaptureScene() {
            super(R.layout.overlay_ctrl_capture);
        }

        @Override
        public void onCreateView(@NotNull View view, @NotNull ControlPanelOverlay2 controller) {
            view.findViewById(R.id.button_capture).setOnClickListener(v -> {
                if (ocrInitializer.getStatus() != AsyncTask.Status.FINISHED) {
                    Toast.makeText(getContext(), "OCR Engine still not ready", Toast.LENGTH_SHORT).show();
                } else if (progressOverlay.getViewport().getVisibility() == View.VISIBLE) {
                    Toast.makeText(getContext(), "Previous capture is still processing!", Toast.LENGTH_SHORT).show();
                } else {
                    OverlayServiceUtil.INSTANCE.captureScreen();
                }
            });
        }
    }

    private static class OcrInitializer extends AsyncTask<Void, Void, Void> {
        private final QuizOverlayManager overlayManager;

        private OcrInitializer(QuizOverlayManager overlayManager) {
            this.overlayManager = overlayManager;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            overlayManager.featureDetector = new FeatureDetector(overlayManager.getContext());
            return null;
        }

        @Override
        protected void onCancelled() {
            if (overlayManager.featureDetector != null) {
                overlayManager.featureDetector.freeResources();
                overlayManager.featureDetector = null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(overlayManager.getContext(), "OCR Engine ready", Toast.LENGTH_SHORT).show();
        }
    }

}
