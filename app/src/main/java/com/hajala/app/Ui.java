package com.hajala.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Shared look-and-feel helpers: colors, press animation, buttons, chips, dialogs. */
final class Ui {
    static final int BG = 0xFF0E0A12;
    static final int SURFACE = 0xFF1E1119;
    static final int PANEL_DEEP = 0xFF3A0F25;
    static final int PINK = 0xFFF48FB1;
    static final int MAROON = 0xFF7A1535;
    static final int BORDER = 0xFF5A2442;
    static final int OUTLINE = 0xFF8A5A72;
    static final int TEXT = 0xFFF3E6EE;
    static final int MUTED = 0xFFB8A6B0;
    static final int ERROR = 0xFFFF8A9B;
    static final int ON_PINK = 0xFF1A0B14;

    /** Background thread pool for decoding images. */
    static final ExecutorService IO = Executors.newFixedThreadPool(2);

    private Ui() {}

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    // ---------------------------------------------------------------- drawables

    static GradientDrawable rounded(Context c, int fill, int stroke, float strokeDp, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(c, radiusDp));
        if (strokeDp > 0) d.setStroke(Math.max(1, dp(c, strokeDp)), stroke);
        return d;
    }

    static GradientDrawable dashed(Context c, int fill, int stroke, float strokeDp, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(c, radiusDp));
        d.setStroke(Math.max(1, dp(c, strokeDp)), stroke, dp(c, 6), dp(c, 4));
        return d;
    }

    /** Wraps [content] in a touch ripple clipped to a rounded rectangle. */
    static Drawable ripple(Context c, Drawable content, float radiusDp) {
        GradientDrawable mask = new GradientDrawable();
        mask.setColor(Color.WHITE);
        mask.setCornerRadius(dp(c, radiusDp));
        return new RippleDrawable(ColorStateList.valueOf(0x44FFFFFF), content, mask);
    }

    // ---------------------------------------------------------------- press animation

    /**
     * Gives a view a tactile press animation: it shrinks while pressed and springs back on release.
     * The touch listener returns false so normal click handling keeps working.
     */
    static void press(View v) {
        v.setOnTouchListener((view, e) -> {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    view.animate().cancel();
                    view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(90)
                            .setInterpolator(new DecelerateInterpolator()).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.animate().cancel();
                    view.animate().scaleX(1f).scaleY(1f).setDuration(240)
                            .setInterpolator(new OvershootInterpolator(3f)).start();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    /** Makes screen readers announce the view as a button. */
    static void asButton(View v) {
        v.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName(Button.class.getName());
            }
        });
    }

    // ---------------------------------------------------------------- views

    static TextView text(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setLineSpacing(dp(c, 3), 1f);
        return t;
    }

    static ImageView icon(Context c, int res, int color, int sizeDp) {
        ImageView iv = new ImageView(c);
        iv.setImageResource(res);
        iv.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setMinimumWidth(dp(c, sizeDp));
        iv.setMinimumHeight(dp(c, sizeDp));
        iv.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return iv;
    }

    /** A full-width action button: optional icon + label, ripple, and press animation. */
    static LinearLayout button(Context c, String label, int iconRes, boolean primary) {
        LinearLayout b = new LinearLayout(c);
        b.setOrientation(LinearLayout.HORIZONTAL);
        b.setGravity(Gravity.CENTER);
        b.setClickable(true);
        b.setFocusable(true);
        b.setPadding(dp(c, 16), 0, dp(c, 16), 0);
        int fg = primary ? Color.WHITE : TEXT;
        if (iconRes != 0) {
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(c, 22), dp(c, 22));
            ip.setMarginEnd(dp(c, 10));
            b.addView(icon(c, iconRes, fg, 22), ip);
        }
        TextView t = text(c, label, 16, fg, true);
        t.setGravity(Gravity.CENTER);
        b.addView(t, new LinearLayout.LayoutParams(-2, -2));
        GradientDrawable bg = primary
                ? rounded(c, MAROON, PINK, 2, 18)
                : rounded(c, Color.TRANSPARENT, OUTLINE, 1, 18);
        b.setBackground(ripple(c, bg, 18));
        asButton(b);
        press(b);
        return b;
    }

    /** Round icon-only button (used for "back"). */
    static FrameLayout iconButton(Context c, int res, String description) {
        FrameLayout f = new FrameLayout(c);
        f.setClickable(true);
        f.setFocusable(true);
        f.setContentDescription(description);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(SURFACE);
        bg.setStroke(dp(c, 1), BORDER);
        GradientDrawable mask = new GradientDrawable();
        mask.setShape(GradientDrawable.OVAL);
        mask.setColor(Color.WHITE);
        f.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44FFFFFF), bg, mask));
        f.addView(icon(c, res, PINK, 22), new FrameLayout.LayoutParams(dp(c, 22), dp(c, 22), Gravity.CENTER));
        asButton(f);
        press(f);
        return f;
    }

    static TextView chip(Context c, String label, boolean selected) {
        TextView t = new TextView(c);
        t.setText(label);
        t.setTextSize(14);
        t.setGravity(Gravity.CENTER);
        t.setClickable(true);
        t.setFocusable(true);
        t.setMinHeight(dp(c, 44));
        t.setPadding(dp(c, 18), 0, dp(c, 18), 0);
        styleChip(c, t, selected);
        asButton(t);
        press(t);
        return t;
    }

    static void styleChip(Context c, TextView t, boolean selected) {
        t.setTextColor(selected ? ON_PINK : TEXT);
        t.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
        t.setBackground(ripple(c, rounded(c, selected ? PINK : SURFACE, selected ? PINK : BORDER, 1, 22), 22));
        t.setSelected(selected);
    }

    /** The "+ تصنيف" chip: dashed outline, used to add a custom category. */
    static TextView addChip(Context c, String label) {
        TextView t = new TextView(c);
        t.setText(label);
        t.setTextSize(14);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setTextColor(PINK);
        t.setGravity(Gravity.CENTER);
        t.setClickable(true);
        t.setFocusable(true);
        t.setMinHeight(dp(c, 44));
        t.setPadding(dp(c, 16), 0, dp(c, 16), 0);
        t.setBackground(ripple(c, dashed(c, Color.TRANSPARENT, PINK, 1.5f, 22), 22));
        asButton(t);
        press(t);
        return t;
    }

    static TextView pill(Context c, String label, int fg, int fill, int stroke) {
        TextView t = new TextView(c);
        t.setText(label);
        t.setTextSize(11);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setTextColor(fg);
        t.setPadding(dp(c, 9), dp(c, 3), dp(c, 9), dp(c, 3));
        t.setBackground(rounded(c, fill, stroke, stroke == 0 ? 0 : 1, 10));
        return t;
    }

    static EditText input(Context c, String hint, boolean multiLine) {
        final EditText e = new EditText(c);
        e.setHint(hint);
        e.setHintTextColor(MUTED);
        e.setTextColor(Color.WHITE);
        e.setTextSize(15);
        e.setPadding(dp(c, 14), dp(c, 12), dp(c, 14), dp(c, 12));
        e.setBackground(rounded(c, SURFACE, BORDER, 1, 14));
        e.setOnFocusChangeListener((v, hasFocus) ->
                v.setBackground(rounded(c, SURFACE, hasFocus ? PINK : BORDER, hasFocus ? 2 : 1, 14)));
        if (multiLine) {
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            e.setMinLines(3);
            e.setMaxLines(6);
            e.setGravity(Gravity.TOP | Gravity.START);
        } else {
            e.setInputType(InputType.TYPE_CLASS_TEXT);
            e.setSingleLine(true);
            e.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }
        return e;
    }

    // ---------------------------------------------------------------- dialogs / toast

    static AlertDialog.Builder dialog(Context c) {
        return new AlertDialog.Builder(c, android.R.style.Theme_Material_Dialog_Alert);
    }

    /** Shows the dialog in RTL with pink buttons and returns it. */
    static AlertDialog show(AlertDialog.Builder b) {
        AlertDialog d = b.create();
        d.show();
        if (d.getWindow() != null) d.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        int[] which = {AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEGATIVE, AlertDialog.BUTTON_NEUTRAL};
        for (int w : which) {
            Button btn = d.getButton(w);
            if (btn != null) btn.setTextColor(PINK);
        }
        return d;
    }

    static void toast(Context c, String s) {
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
    }

    // ---------------------------------------------------------------- images

    /** Decodes an image from a content Uri, down-sampled close to the requested size. Returns null on failure. */
    static Bitmap decode(Context c, String uriString, int reqW, int reqH) {
        try {
            Uri uri = Uri.parse(uriString);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            InputStream in = c.getContentResolver().openInputStream(uri);
            if (in == null) return null;
            try {
                BitmapFactory.decodeStream(in, null, o);
            } finally {
                in.close();
            }
            int sample = 1;
            while (o.outWidth / (sample * 2) >= reqW && o.outHeight / (sample * 2) >= reqH) sample *= 2;
            o.inJustDecodeBounds = false;
            o.inSampleSize = sample;
            InputStream in2 = c.getContentResolver().openInputStream(uri);
            if (in2 == null) return null;
            try {
                return BitmapFactory.decodeStream(in2, null, o);
            } finally {
                in2.close();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
