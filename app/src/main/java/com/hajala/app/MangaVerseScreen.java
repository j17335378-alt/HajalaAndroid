package com.hajala.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/** The MangaVerse section: publish button, type filters and a grid of the user's works. */
final class MangaVerseScreen {
    interface Host {
        void openPublish();
    }

    private static final String[] FILTER_IDS = {"all", Store.MANGA, Store.MANHWA, Store.ONESHOT};
    private static final String[] FILTER_LABELS = {"الكل", "مانغا", "مانهوا", "ونشوت"};
    private static final int[][] TONES = {
            {0xFFD81B60, 0xFF5B1236, 0xFF1E0A18},
            {0xFF7E57C2, 0xFF3A1F63, 0xFF160C26},
            {0xFFF06292, 0xFF8A1F4F, 0xFF2A0E1E},
            {0xFF5C6BC0, 0xFF2B2F6B, 0xFF0F1030},
    };

    private final Activity act;
    private final Store store;
    private final Host host;
    private String filter = "all";
    private final List<TextView> chipViews = new ArrayList<>();
    private LinearLayout grid;

    MangaVerseScreen(Activity act, Store store, Host host) {
        this.act = act;
        this.store = store;
        this.host = host;
    }

    private int dp(float v) {
        return Ui.dp(act, v);
    }

    /** Builds a fresh view tree for this section. */
    View build() {
        Context c = act;
        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);

        TextView sub = Ui.text(c, "انشر مانغا ومانهوا وونشوت", 14, Ui.MUTED, false);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.bottomMargin = dp(14);
        root.addView(sub, subLp);

        LinearLayout cta = Ui.button(c, "نشر عمل جديد", R.drawable.ic_plus, true);
        cta.setOnClickListener(v -> host.openPublish());
        LinearLayout.LayoutParams ctaLp = new LinearLayout.LayoutParams(-1, dp(58));
        ctaLp.bottomMargin = dp(16);
        root.addView(cta, ctaLp);

        HorizontalScrollView hs = new HorizontalScrollView(c);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout chipsRow = new LinearLayout(c);
        chipsRow.setOrientation(LinearLayout.HORIZONTAL);
        chipViews.clear();
        for (int i = 0; i < FILTER_IDS.length; i++) {
            final String id = FILTER_IDS[i];
            TextView chip = Ui.chip(c, FILTER_LABELS[i], id.equals(filter));
            chip.setOnClickListener(v -> {
                filter = id;
                refreshChips();
                refreshGrid();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.setMarginEnd(dp(8));
            chipsRow.addView(chip, lp);
            chipViews.add(chip);
        }
        hs.addView(chipsRow);
        LinearLayout.LayoutParams hsLp = new LinearLayout.LayoutParams(-1, -2);
        hsLp.bottomMargin = dp(18);
        root.addView(hs, hsLp);

        TextView title = Ui.text(c, "أعمالي", 19, Ui.PINK, true);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(-1, -2);
        tLp.bottomMargin = dp(12);
        root.addView(title, tLp);

        grid = new LinearLayout(c);
        grid.setOrientation(LinearLayout.VERTICAL);
        root.addView(grid, new LinearLayout.LayoutParams(-1, -2));

        refreshGrid();
        return root;
    }

    /** Re-reads the works from storage and redraws the grid (if the section is on screen). */
    void refresh() {
        if (grid != null) refreshGrid();
    }

    private void refreshChips() {
        for (int i = 0; i < chipViews.size(); i++) {
            Ui.styleChip(act, chipViews.get(i), FILTER_IDS[i].equals(filter));
        }
    }

    private void refreshGrid() {
        grid.removeAllViews();
        List<Store.Work> all = store.works();
        List<Store.Work> shown = new ArrayList<>();
        for (Store.Work w : all) {
            if ("all".equals(filter) || filter.equals(w.type)) shown.add(w);
        }
        if (shown.isEmpty()) {
            grid.addView(emptyState(all.isEmpty()), new LinearLayout.LayoutParams(-1, -2));
            return;
        }
        for (int i = 0; i < shown.size(); i += 2) {
            LinearLayout row = new LinearLayout(act);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < 2; j++) {
                int idx = i + j;
                View cell = idx < shown.size() ? card(shown.get(idx), idx) : new View(act);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
                if (j == 0) lp.setMarginEnd(dp(7));
                else lp.setMarginStart(dp(7));
                row.addView(cell, lp);
            }
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, -2);
            rowLp.bottomMargin = dp(14);
            grid.addView(row, rowLp);
        }
    }

    private View emptyState(boolean noWorksAtAll) {
        Context c = act;
        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(20), dp(32), dp(20), dp(32));
        box.setBackground(Ui.dashed(c, Ui.SURFACE, Ui.BORDER, 1.5f, 18));

        box.addView(Ui.icon(c, R.drawable.ic_book, Ui.PINK, 40), new LinearLayout.LayoutParams(dp(40), dp(40)));
        TextView t = Ui.text(c, noWorksAtAll ? "لا توجد أعمال بعد" : "لا توجد أعمال في هذا التصنيف",
                17, Ui.TEXT, true);
        t.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(-2, -2);
        tLp.topMargin = dp(12);
        box.addView(t, tLp);
        TextView d = Ui.text(c, noWorksAtAll
                ? "ابدأ بنشر أول مانغا أو مانهوا أو ونشوت."
                : "جرّب تصنيفًا آخر أو انشر عملًا جديدًا.", 14, Ui.MUTED, false);
        d.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(-2, -2);
        dLp.topMargin = dp(6);
        box.addView(d, dLp);
        return box;
    }

    private View card(final Store.Work w, int idx) {
        Context c = act;
        AspectFrame card = new AspectFrame(c);
        int[] tone = TONES[idx % TONES.length];
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, tone);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), Ui.BORDER);
        card.setBackground(bg);
        card.setClipToOutline(true);
        card.setForeground(Ui.ripple(c, null, 16));

        if (!w.cover.isEmpty()) {
            final ImageView iv = new ImageView(c);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            card.addView(iv, new FrameLayout.LayoutParams(-1, -1));
            final String cover = w.cover;
            Ui.IO.execute(() -> {
                final Bitmap bmp = Ui.decode(act, cover, 400, 533);
                if (bmp != null) iv.post(() -> iv.setImageBitmap(bmp));
            });
        }

        // bottom title over a dark scrim
        GradientDrawable scrim = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {0x000E0A12, 0xF00E0A12});
        TextView title = Ui.text(c, w.title, 15, Color.WHITE, true);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setPadding(dp(12), dp(34), dp(12), dp(12));
        title.setBackground(scrim);
        card.addView(title, new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM));

        // type pill (start) and status pill (end)
        TextView type = Ui.pill(c, Store.typeLabel(w.type), Ui.TEXT, 0xD10E0A12, 0);
        FrameLayout.LayoutParams typeLp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.START);
        typeLp.setMargins(dp(10), dp(10), dp(10), 0);
        card.addView(type, typeLp);

        boolean published = Store.PUBLISHED.equals(w.status);
        TextView status = published
                ? Ui.pill(c, "منشور", Ui.ON_PINK, Ui.PINK, 0)
                : Ui.pill(c, "مسودة", Ui.TEXT, 0xD10E0A12, Ui.OUTLINE);
        FrameLayout.LayoutParams stLp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.END);
        stLp.setMargins(dp(10), dp(10), dp(10), 0);
        card.addView(status, stLp);

        card.setContentDescription(w.title + "، " + Store.typeLabel(w.type) + "، " + Store.statusLabel(w.status));
        card.setOnClickListener(v -> showDetails(w));
        Ui.asButton(card);
        Ui.press(card);
        return card;
    }

    private void showDetails(final Store.Work w) {
        boolean published = Store.PUBLISHED.equals(w.status);
        StringBuilder msg = new StringBuilder();
        msg.append(Store.typeLabel(w.type)).append(" • ").append(Store.statusLabel(w.status));
        if (!w.genres.isEmpty()) msg.append("\n").append(TextUtils.join("، ", w.genres));
        if (!w.desc.isEmpty()) msg.append("\n\n").append(w.desc);

        AlertDialog.Builder b = Ui.dialog(act)
                .setTitle(w.title)
                .setMessage(msg.toString())
                .setPositiveButton("إغلاق", null)
                .setNeutralButton(published ? "تحويل لمسودة" : "نشر", (d, i) -> {
                    store.setStatus(w.id, published ? Store.DRAFT : Store.PUBLISHED);
                    refreshGrid();
                })
                .setNegativeButton("حذف", (d, i) -> confirmDelete(w));
        Ui.show(b);
    }

    private void confirmDelete(final Store.Work w) {
        AlertDialog.Builder b = Ui.dialog(act)
                .setTitle("حذف العمل؟")
                .setMessage("سيتم حذف «" + w.title + "» من مكتبتك على هذا الجهاز.")
                .setNegativeButton("إلغاء", null)
                .setPositiveButton("حذف", (d, i) -> {
                    store.deleteWork(w.id);
                    refreshGrid();
                    Ui.toast(act, "تم حذف العمل");
                });
        Ui.show(b);
    }

    /** A frame whose height is always 4/3 of its width (3:4 cover ratio). */
    private static final class AspectFrame extends FrameLayout {
        AspectFrame(Context c) {
            super(c);
            setClickable(true);
            setFocusable(true);
        }

        @Override
        protected void onMeasure(int widthSpec, int heightSpec) {
            int width = MeasureSpec.getSize(widthSpec);
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(width * 4 / 3, MeasureSpec.EXACTLY));
        }
    }
}
