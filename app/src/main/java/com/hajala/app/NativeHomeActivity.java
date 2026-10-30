package com.hajala.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import java.util.ArrayList;
import java.util.List;

/** Native Android UI for Hajala. This intentionally does not load the website in WebView. */
public class NativeHomeActivity extends Activity {
    private static final int PICK_MEDIA = 7101;
    private static final int PICK_COVER = 7102;

    private static final String HOME = "الرئيسية";
    private static final String MANGA = "مانغا";
    private static final String STUDIO = "الاستوديو";
    private static final String FRIENDS = "الأصدقاء";
    private static final String PROFILE = "ملفي";
    private static final String[] TABS = {HOME, MANGA, STUDIO, FRIENDS, PROFILE};
    private static final int[] TAB_ICONS = {
            R.drawable.ic_home, R.drawable.ic_book, R.drawable.ic_add_circle,
            R.drawable.ic_people, R.drawable.ic_person};

    private FrameLayout container;
    private LinearLayout content;
    private ScrollView scroll;
    private final ImageView[] tabIcons = new ImageView[TABS.length];
    private final TextView[] tabLabels = new TextView[TABS.length];
    private String active = HOME;
    private final List<Uri> selectedMedia = new ArrayList<>();

    private Store store;
    private MangaVerseScreen mangaVerse;
    private PublishScreen publish;
    private View publishView;
    private Object backCallback;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Window w = getWindow();
        w.setStatusBarColor(Ui.BG);
        w.setNavigationBarColor(Ui.BG);
        w.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        store = new Store(this);
        mangaVerse = new MangaVerseScreen(this, store, this::openPublish);
        buildShell();
        showPage(HOME);
    }

    private int dp(float v) {
        return Ui.dp(this, v);
    }

    // ------------------------------------------------------------------ shell

    private void buildShell() {
        container = new FrameLayout(this);
        container.setBackgroundColor(Ui.BG);
        if (Build.VERSION.SDK_INT >= 35) {
            // From Android 15 the window is edge-to-edge: keep content clear of the bars and the keyboard.
            container.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                Insets ime = insets.getInsets(WindowInsets.Type.ime());
                v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
                return WindowInsets.CONSUMED;
            });
        }

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(Ui.BG);

        // brand bar
        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        brand.setGravity(Gravity.CENTER);
        brand.setPadding(dp(16), dp(16), dp(16), dp(12));
        brand.addView(Ui.text(this, "حجــالة", 27, Ui.PINK, true), new LinearLayout.LayoutParams(-2, -2));
        LinearLayout.LayoutParams heartLp = new LinearLayout.LayoutParams(dp(26), dp(26));
        heartLp.setMarginStart(dp(8));
        brand.addView(Ui.icon(this, R.drawable.ic_heart, Ui.PINK, 26), heartLp);
        shell.addView(brand, new LinearLayout.LayoutParams(-1, -2));

        View line = new View(this);
        line.setBackgroundColor(Ui.MAROON);
        shell.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));

        // page content
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(12), dp(16), dp(16));
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.addView(content, new FrameLayout.LayoutParams(-1, -2));
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        // bottom navigation
        View navLine = new View(this);
        navLine.setBackgroundColor(Ui.MAROON);
        shell.addView(navLine, new LinearLayout.LayoutParams(-1, dp(1)));
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setBackgroundColor(Ui.SURFACE);
        nav.setPadding(dp(4), dp(6), dp(4), dp(6));
        for (int i = 0; i < TABS.length; i++) {
            final String tab = TABS[i];
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setClickable(true);
            item.setFocusable(true);
            item.setContentDescription(tab);
            item.setBackground(Ui.ripple(this, null, 16));
            ImageView ic = Ui.icon(this, TAB_ICONS[i], Ui.MUTED, 24);
            TextView lb = Ui.text(this, tab, 11, Ui.MUTED, false);
            lb.setGravity(Gravity.CENTER);
            item.addView(ic, new LinearLayout.LayoutParams(dp(24), dp(24)));
            LinearLayout.LayoutParams lbLp = new LinearLayout.LayoutParams(-2, -2);
            lbLp.topMargin = dp(4);
            item.addView(lb, lbLp);
            item.setOnClickListener(v -> showPage(tab));
            Ui.asButton(item);
            Ui.press(item);
            tabIcons[i] = ic;
            tabLabels[i] = lb;
            nav.addView(item, new LinearLayout.LayoutParams(0, dp(56), 1f));
        }
        shell.addView(nav, new LinearLayout.LayoutParams(-1, -2));

        container.addView(shell, new FrameLayout.LayoutParams(-1, -1));
        setContentView(container);
    }

    private void updateTabs() {
        for (int i = 0; i < TABS.length; i++) {
            boolean on = TABS[i].equals(active);
            int color = on ? Ui.PINK : Ui.MUTED;
            tabIcons[i].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            tabLabels[i].setTextColor(color);
            tabLabels[i].setTypeface(Typeface.DEFAULT, on ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    // ------------------------------------------------------------------ pages

    private void add(View v) {
        content.addView(v, new LinearLayout.LayoutParams(-1, -2));
    }

    private void showPage(String page) {
        active = page;
        content.removeAllViews();
        updateTabs();
        scroll.scrollTo(0, 0);
        content.setAlpha(0f);
        content.animate().alpha(1f).setDuration(160).start();

        TextView heading = Ui.text(this, MANGA.equals(page) ? "MangaVerse" : page, 25, Ui.PINK, true);
        heading.setPadding(0, dp(8), 0, dp(8));
        add(heading);

        if (HOME.equals(page)) {
            add(Ui.text(this, "مرحبًا بك في حجالة", 21, Color.WHITE, true));
            TextView sub = Ui.text(this, "واجهة أندرويد أصلية قيد التطوير", 14, Ui.MUTED, false);
            sub.setPadding(0, dp(4), 0, dp(14));
            add(sub);
            addCard("المانغا والمانهوا", "تابع القراءة واستكشف مكتبتك", "فتح المكتبة", () -> showPage(MANGA));
            addCard("الاستوديو", "اختر صورك وفيديوهاتك من الهاتف", "فتح الاستوديو", () -> showPage(STUDIO));
            addCard("الفيديوهات", "سيتم ربط المشغل الأصلي في المرحلة التالية", "اختيار فيديو",
                    () -> pickMedia("video/*"));
            addCard("تيك جيل", "من دون نظام البوتات", "فتح القسم",
                    () -> Ui.toast(this, "تم تعطيل نظام البوتات في هذه الواجهة الأولية"));
        } else if (STUDIO.equals(page)) {
            add(Ui.text(this, "ملفاتك المحلية", 17, Color.WHITE, true));
            TextView hint = Ui.text(this,
                    "اختَر الصور أو الفيديوهات من منتقي أندرويد. لن نطلب صلاحية الوصول لكل ملفات الهاتف.",
                    14, Ui.MUTED, false);
            hint.setPadding(0, dp(4), 0, dp(14));
            add(hint);
            addButton("إضافة صور وفيديوهات", R.drawable.ic_plus, () -> pickMedia("image/*,video/*"), true);
            addButton("اختيار صور فقط", 0, () -> pickMedia("image/*"), false);
            addButton("اختيار فيديو فقط", 0, () -> pickMedia("video/*"), false);
            TextView count = Ui.text(this, "العناصر المختارة في هذه الجلسة: " + selectedMedia.size(),
                    16, Ui.PINK, true);
            count.setPadding(0, dp(6), 0, dp(10));
            add(count);
            for (Uri uri : selectedMedia) {
                TextView f = Ui.text(this, "•  " + displayName(uri), 13, Color.WHITE, false);
                f.setPadding(dp(12), dp(10), dp(12), dp(10));
                f.setBackground(Ui.rounded(this, Ui.SURFACE, Ui.BORDER, 1, 12));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
                lp.bottomMargin = dp(7);
                content.addView(f, lp);
            }
        } else if (MANGA.equals(page)) {
            add(mangaVerse.build());
        } else if (FRIENDS.equals(page)) {
            add(Ui.text(this, "الأصدقاء والرسائل", 18, Color.WHITE, true));
            addCard("الأصدقاء", "واجهة أولية", "قريبًا",
                    () -> Ui.toast(this, "سيتم ربط هذه الوظيفة بعد نقل بيانات الموقع"));
            addCard("صندوق الوارد", "واجهة أولية", "فتح",
                    () -> Ui.toast(this, "صندوق الوارد يحتاج ربط البيانات"));
        } else if (PROFILE.equals(page)) {
            add(Ui.text(this, "ملفي الشخصي", 18, Color.WHITE, true));
            addCard("إعدادات حجالة", "المظهر والتفضيلات", "الإعدادات",
                    () -> Ui.toast(this, "الإعدادات الأصلية قيد النقل"));
        }
        updateBack();
    }

    private void addCard(String title, String desc, String action, Runnable run) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(14), dp(15), dp(14));
        card.setBackground(Ui.rounded(this, Ui.SURFACE, Ui.BORDER, 1, 18));
        card.addView(Ui.text(this, title, 19, Ui.PINK, true), new LinearLayout.LayoutParams(-1, -2));
        TextView d = Ui.text(this, desc, 14, Ui.MUTED, false);
        d.setPadding(0, dp(6), 0, dp(12));
        card.addView(d, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout b = Ui.button(this, action, 0, true);
        b.setOnClickListener(v -> run.run());
        card.addView(b, new LinearLayout.LayoutParams(-1, dp(52)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        content.addView(card, lp);
    }

    private void addButton(String label, int iconRes, Runnable run, boolean primary) {
        LinearLayout b = Ui.button(this, label, iconRes, primary);
        b.setOnClickListener(v -> run.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(52));
        lp.bottomMargin = dp(10);
        content.addView(b, lp);
    }

    // ------------------------------------------------------------------ publish screen

    private void openPublish() {
        if (publishView != null) return;
        publish = new PublishScreen(this, store, new PublishScreen.Host() {
            @Override
            public void closePublish(boolean saved) {
                NativeHomeActivity.this.closePublish(saved);
            }

            @Override
            public void pickCover() {
                NativeHomeActivity.this.pickCover();
            }
        });
        publishView = publish.build();
        container.addView(publishView, new FrameLayout.LayoutParams(-1, -1));
        publishView.setAlpha(0f);
        publishView.setTranslationX(-dp(36));
        publishView.animate().alpha(1f).translationX(0f).setDuration(220)
                .setInterpolator(new DecelerateInterpolator()).start();
        updateBack();
    }

    private void closePublish(boolean saved) {
        if (publishView == null) return;
        hideKeyboard();
        final View v = publishView;
        publishView = null;
        publish = null;
        v.animate().alpha(0f).translationX(-dp(36)).setDuration(180)
                .withEndAction(() -> container.removeView(v)).start();
        if (saved) showPage(MANGA);
        updateBack();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View f = getCurrentFocus();
        if (imm != null && f != null) imm.hideSoftInputFromWindow(f.getWindowToken(), 0);
    }

    private void pickCover() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i, PICK_COVER);
    }

    // ------------------------------------------------------------------ back button

    /** Handles "back": closes the publish screen, or returns to Home from another tab. */
    private boolean handleBack() {
        if (publishView != null) {
            closePublish(false);
            return true;
        }
        if (!HOME.equals(active)) {
            showPage(HOME);
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (!handleBack()) super.onBackPressed();
    }

    /** On Android 13+ the back gesture is delivered through a callback, registered only when we need it. */
    private void updateBack() {
        boolean need = publishView != null || !HOME.equals(active);
        if (Build.VERSION.SDK_INT >= 33) {
            if (need && backCallback == null) {
                backCallback = BackApi33.register(this, this::handleBack);
            } else if (!need && backCallback != null) {
                BackApi33.unregister(this, backCallback);
                backCallback = null;
            }
        }
    }

    private static final class BackApi33 {
        static Object register(Activity a, final Runnable action) {
            OnBackInvokedCallback cb = action::run;
            a.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, cb);
            return cb;
        }

        static void unregister(Activity a, Object cb) {
            a.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback((OnBackInvokedCallback) cb);
        }
    }

    // ------------------------------------------------------------------ media picking (Studio)

    private void pickMedia(String mime) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, mime.split(","));
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(i, PICK_MEDIA);
    }

    private void keepAccess(Intent data, Uri uri) {
        try {
            int flags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(uri, flags != 0 ? flags : Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
            // some providers do not allow persistable access; the Uri still works for this session
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == PICK_COVER) {
            Uri u = data.getData();
            if (u != null && publish != null) {
                keepAccess(data, u);
                publish.onCoverPicked(u);
            }
            return;
        }

        if (requestCode == PICK_MEDIA) {
            if (data.getClipData() != null) {
                for (int n = 0; n < data.getClipData().getItemCount(); n++) {
                    Uri u = data.getClipData().getItemAt(n).getUri();
                    keepAccess(data, u);
                    selectedMedia.add(u);
                }
            } else if (data.getData() != null) {
                Uri u = data.getData();
                keepAccess(data, u);
                selectedMedia.add(u);
            }
            showPage(STUDIO);
            Ui.toast(this, "تم اختيار الملفات. الحفظ الدائم وربط النشر قيد التنفيذ.");
        }
    }

    private String displayName(Uri uri) {
        String s = uri.getLastPathSegment();
        return s == null ? uri.toString() : s;
    }
}
