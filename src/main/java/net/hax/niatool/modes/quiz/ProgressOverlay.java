package net.hax.niatool.modes.quiz;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import net.hax.niatool.R;

class ProgressOverlay {

    private final View viewport;
    private final ProgressBar progressBar;
    private final TextView progressDescription;

    ProgressOverlay(Context context) {
        LayoutInflater systemInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewport = systemInflater.inflate(R.layout.overlay_progress, null);
        progressBar = (ProgressBar) viewport.findViewById(android.R.id.progress);
        progressDescription = (TextView) viewport.findViewById(android.R.id.text1);
    }

    View getViewport() {
        return viewport;
    }

    public void setProgress(int amount, String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress(amount, true);
        } else {
            progressBar.setProgress(amount);
        }
        progressDescription.setText(description);
    }

}
