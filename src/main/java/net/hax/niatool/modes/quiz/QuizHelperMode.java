package net.hax.niatool.modes.quiz;

import android.content.Context;
import android.content.res.Resources;
import net.hax.niatool.modes.ModeRegistry;
import net.hax.niatool.modes.OperationMode;
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
        return new ReadAndSearchTask(overlayManager);
    }

}
