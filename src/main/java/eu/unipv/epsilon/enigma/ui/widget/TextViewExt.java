package eu.unipv.epsilon.enigma.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import eu.unipv.epsilon.enigma.ui.util.FontLoader;
import net.hax.niatool.R;

/**
 * An extended {@link AppCompatTextView TextView} capable of loading fonts from application assets.
 */
public class TextViewExt extends AppCompatTextView {

    public TextViewExt(Context context) {
        super(context);
        loadTypefaceAttributes(null, 0);
    }

    public TextViewExt(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadTypefaceAttributes(attrs, 0);
    }

    public TextViewExt(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadTypefaceAttributes(attrs, defStyleAttr);
    }

    private void loadTypefaceAttributes(AttributeSet attrs, int defStyleAttr) {
        // Load attributes, try first from local attributes, then textAppearance
        final TypedArray localAttrs = getContext().obtainStyledAttributes(
                attrs, R.styleable.TextViewExt, defStyleAttr, 0);

        final TypedArray textAppearanceArray = getContext().obtainStyledAttributes(
                attrs, new int[]{android.R.attr.textAppearance}, defStyleAttr, 0);
        final int bId = textAppearanceArray.getResourceId(0, -1);
        textAppearanceArray.recycle();

        final TypedArray appearanceAttrs = bId == -1 ? null :
                getContext().obtainStyledAttributes(bId, R.styleable.TextViewExt);

        try {
            String fontFamily = localAttrs.getString(R.styleable.TextViewExt_localFontFamily);
            if (appearanceAttrs != null && fontFamily == null)
                fontFamily = appearanceAttrs.getString(R.styleable.TextViewExt_localFontFamily);

            int fontStyle = localAttrs.getInt(R.styleable.TextViewExt_localFontStyle, -1);
            if (appearanceAttrs != null && fontStyle == -1)
                fontStyle = appearanceAttrs.getInt(R.styleable.TextViewExt_localFontStyle, Typeface.NORMAL);

            Typeface font;
            if (fontFamily != null) {
                final FontLoader fontLoader = new FontLoader(getContext().getAssets());

                String fontFamilyExt = localAttrs.getString(R.styleable.TextViewExt_localFontFamilySuffix);
                if (appearanceAttrs != null && fontFamilyExt == null)
                    fontFamilyExt = appearanceAttrs.getString(R.styleable.TextViewExt_localFontFamilySuffix);

                font = fontLoader.loadFont(fontFamily, fontFamilyExt);
            } else
                font = FontLoader.DEFAULT_FONT;

            setTypeface(font, fontStyle);

        } finally {
            localAttrs.recycle();
            if (appearanceAttrs != null) appearanceAttrs.recycle();
        }
    }

}
