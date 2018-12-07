package net.hax.niatool.modes.quiz;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.v4.view.GravityCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import net.hax.niatool.OverlayServiceUtil;
import net.hax.niatool.R;
import net.hax.niatool.overlay.ControlPanelOverlay2;
import net.hax.niatool.overlay.OverlayViewManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class QuizOverlayManager extends OverlayViewManager {

    private static final int PROGRESS_MARGIN_BOTTOM_DP = 100;
    private static final int RESULT_MARGIN_RIGHT_DP = 60;

    private ProgressOverlay progressOverlay = null;
    private ResultDisplayOverlay[] resultOverlays = new ResultDisplayOverlay[3];

    public QuizOverlayManager(@NotNull Context context) {
        super(context);
    }

    @Override
    public void configureControlOverlay(@NotNull ControlPanelOverlay2 controlOverlay) {
        controlOverlay.addScene(new CaptureScene());
        controlOverlay.switchScene(CaptureScene.class);
    }

    void onTaskStart() {
        clearResults();

        if (progressOverlay == null) {
            progressOverlay = new ProgressOverlay(getContext());
            getWindowManager().addView(progressOverlay.getViewport(), makeProgressLayoutParams());
        }
        progressOverlay.setProgress(0, null);
    }

    void onProgressUpdate(int percentage, String description) {
        if (progressOverlay != null)
            progressOverlay.setProgress(percentage, description);
    }

    void onResult(int[] values, int[] yCoords) {
        System.out.println(Arrays.toString(yCoords));
        getWindowManager().removeView(progressOverlay.getViewport());
        progressOverlay = null;

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

            resultOverlays[i].setText(String.valueOf(values[i]) + '%');
            getWindowManager().addView(resultOverlays[i].getViewport(),
                    makeResultLayoutParams(yCoords[i], resultOverlays[i]));
        }
    }

    @Override
    public void onProjectionStop() {
        getWindowManager().removeView(progressOverlay.getViewport());
        progressOverlay = null;
        clearResults();

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

    private static class CaptureScene extends ControlPanelOverlay2.Scene {
        public CaptureScene() {
            super(R.layout.overlay_ctrl_capture);
        }

        @Override
        public void onCreateView(@NotNull View view, @NotNull ControlPanelOverlay2 controller) {
            view.findViewById(R.id.button_capture)
                    .setOnClickListener(v -> OverlayServiceUtil.INSTANCE.captureScreen());
        }
    }

}
