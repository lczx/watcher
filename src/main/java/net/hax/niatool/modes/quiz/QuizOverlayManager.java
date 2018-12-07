package net.hax.niatool.modes.quiz;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import net.hax.niatool.OverlayServiceUtil;
import net.hax.niatool.R;
import net.hax.niatool.overlay.ControlPanelOverlay2;
import net.hax.niatool.overlay.OverlayViewManager;
import org.jetbrains.annotations.NotNull;

public class QuizOverlayManager extends OverlayViewManager {

    public QuizOverlayManager(@NotNull Context context) {
        super(context);
    }

    @Override
    public void configureControlOverlay(@NotNull ControlPanelOverlay2 controlOverlay) {
        controlOverlay.addScene(new CaptureScene());
        controlOverlay.switchScene(CaptureScene.class);
    }

    void aggiornaContenuto(int[] p) {
        if (p != null)
            Toast.makeText(getContext(), "Answer1: " + p[0] + "\n Answer2:" + p[1] + "\n Answer3:" + p[2], Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getContext(), "No results founded.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProjectionStart() {
        super.onProjectionStart();
        Toast.makeText(getContext(), "Projection started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProjectionStop() {
        super.onProjectionStop();
        Toast.makeText(getContext(), "Projection stopped", Toast.LENGTH_SHORT).show();
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
