package net.hax.niatool.modes.quiz;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

public class ResultDisplayOverlay {

    private static final int TEXT_HEIGHT_SP = 20;

    private final TextView textView;

    public ResultDisplayOverlay(Context context) {
        textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TEXT_HEIGHT_SP);
        textView.setIncludeFontPadding(false);
        textView.setLineSpacing(0, 0);
    }

    public View getViewport() {
        return textView;
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public int getViewHeight() {
        return (int) textView.getTextSize();
    }

}
