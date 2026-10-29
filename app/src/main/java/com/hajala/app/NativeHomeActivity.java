package com.hajala.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/** Native Android starter UI for Hajala. This intentionally does not load the website in WebView. */
public class NativeHomeActivity extends Activity {
    private static final int PICK_MEDIA = 7101;
    private final int bg = Color.rgb(16, 11, 23);
    private final int panel = Color.rgb(35, 17, 31);
    private final int pink = Color.rgb(242, 145, 183);
    private final int rose = Color.rgb(124, 22, 54);
    private final int gold = Color.rgb(216, 178, 101);
    private LinearLayout root, content, bottom;
    private String active = "الرئيسية";
    private final List<Uri> selectedMedia = new ArrayList<>();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Window w = getWindow();
        w.setStatusBarColor(bg); w.setNavigationBarColor(bg);
        w.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        buildShell(); showPage("الرئيسية");
    }

    private void buildShell() {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(bg);
        TextView brand = new TextView(this); brand.setText("حجــالة  ♡"); brand.setTextColor(pink); brand.setTextSize(27); brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD); brand.setGravity(Gravity.CENTER); brand.setPadding(16,18,16,14);
        root.addView(brand, new LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT));
        FrameLayout line = new FrameLayout(this); line.setBackgroundColor(rose); root.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(16),dp(12),dp(16),dp(14));
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false); scroll.addView(content); root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1));
        bottom = new LinearLayout(this); bottom.setOrientation(LinearLayout.HORIZONTAL); bottom.setGravity(Gravity.CENTER); bottom.setPadding(dp(5),dp(7),dp(5),dp(7)); bottom.setBackground(round(panel, rose, 1, 0)); root.addView(bottom,new LinearLayout.LayoutParams(-1,dp(66)));
        String[] tabs = {"الرئيسية","مانغا","الاستوديو","الأصدقاء","ملفي"}; String[] icons = {"⌂","▤","＋","♧","♙"};
        for (int i=0;i<tabs.length;i++) { final String tab=tabs[i]; LinearLayout item = new LinearLayout(this); item.setOrientation(LinearLayout.VERTICAL); item.setGravity(Gravity.CENTER); TextView icon=new TextView(this); icon.setText(icons[i]); icon.setTextSize(23); icon.setGravity(Gravity.CENTER); icon.setTextColor(tab.equals(active)?pink:Color.LTGRAY); TextView label=new TextView(this); label.setText(tab); label.setTextSize(10); label.setGravity(Gravity.CENTER); label.setTextColor(tab.equals(active)?pink:Color.LTGRAY); item.addView(icon); item.addView(label); item.setOnClickListener(v->{ if(tab.equals("الاستوديو")){active=tab; showPage(tab);} else {active=tab; showPage(tab);} }); bottom.addView(item,new LinearLayout.LayoutParams(0,-1,1)); }
        setContentView(root);
    }

    private void showPage(String page) {
        active=page; content.removeAllViews();
        TextView heading = text(page,25,pink,true); heading.setPadding(0,dp(8),0,dp(8)); content.addView(heading);
        if (page.equals("الرئيسية")) {
            content.addView(text("مرحبًا بك في حجالة",21,Color.WHITE,true));
            content.addView(text("واجهة أندرويد أصلية قيد التطوير",14,Color.LTGRAY,false));
            addCard("المانغا والمانهوا","تابع القراءة واستكشف مكتبتك", "فتح المكتبة", ()->showPage("مانغا"));
            addCard("الاستوديو","اختر صورك وفيديوهاتك من الهاتف", "فتح الاستوديو", ()->showPage("الاستوديو"));
            addCard("الفيديوهات","سيتم ربط المشغل الأصلي في المرحلة التالية", "اختيار فيديو", ()->pickMedia("video/*"));
            addCard("تيك جيل","من دون نظام البوتات", "فتح القسم", ()->toast("تم تعطيل نظام البوتات في هذه الواجهة الأولية"));
        } else if (page.equals("الاستوديو")) {
            content.addView(text("ملفاتك المحلية",17,Color.WHITE,true));
            content.addView(text("اختَر الصور أو الفيديوهات من منتقي أندرويد. لن نطلب صلاحية الوصول لكل ملفات الهاتف.",14,Color.LTGRAY,false));
            addButton("＋  إضافة صور وفيديوهات", ()->pickMedia("image/*,video/*"), true);
            addButton("اختيار صور فقط", ()->pickMedia("image/*"), false);
            addButton("اختيار فيديو فقط", ()->pickMedia("video/*"), false);
            content.addView(text("العناصر المختارة في هذه الجلسة: "+selectedMedia.size(),16,pink,true));
            for (Uri uri:selectedMedia) { TextView f=text("•  "+displayName(uri),13,Color.WHITE,false); f.setPadding(dp(12),dp(10),dp(12),dp(10)); f.setBackground(round(panel,rose,1,12)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(7); content.addView(f,lp); }
        } else if (page.equals("مانغا")) {
            content.addView(text("مكتبة المانغا",18,Color.WHITE,true));
            addCard("قارئ المانغا","سيُنقل عرض الفصول والتكبير إلى واجهة أندرويد أصلية على مراحل.","فتح القارئ",()->toast("قارئ المانغا الأصلي لم يُربط بعد"));
        } else if (page.equals("الأصدقاء")) {
            content.addView(text("الأصدقاء والرسائل",18,Color.WHITE,true));
            addCard("الأصدقاء","واجهة أولية", "قريبًا", ()->toast("سيتم ربط هذه الوظيفة بعد نقل بيانات الموقع"));
            addCard("صندوق الوارد","واجهة أولية", "فتح", ()->toast("صندوق الوارد يحتاج ربط البيانات"));
        } else if (page.equals("ملفي")) {
            content.addView(text("ملفي الشخصي",18,Color.WHITE,true));
            addCard("إعدادات حجالة","المظهر والتفضيلات", "الإعدادات", ()->toast("الإعدادات الأصلية قيد النقل"));
        }
    }

    private void addCard(String title,String desc,String action,Runnable run) {
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(15),dp(14),dp(15),dp(14)); card.setBackground(round(panel,rose,1,18));
        TextView t=text(title,19,pink,true); card.addView(t); TextView d=text(desc,14,Color.LTGRAY,false); d.setPadding(0,dp(6),0,dp(12)); card.addView(d);
        Button b=button(action,true); b.setOnClickListener(v->run.run()); card.addView(b); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(12); content.addView(card,lp);
    }
    private void addButton(String label,Runnable run,boolean primary){ Button b=button(label,primary); b.setOnClickListener(v->run.run()); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52)); lp.bottomMargin=dp(10); content.addView(b,lp); }
    private Button button(String label,boolean primary){ Button b=new Button(this); b.setText(label); b.setTextSize(15); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setBackground(round(primary?rose:panel,primary?pink:rose,primary?2:1,15)); return b; }
    private TextView text(String s,int size,int color,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); t.setGravity(Gravity.RIGHT); t.setLineSpacing(dp(3),1f); return t; }
    private GradientDrawable round(int color,int stroke,int sw,int radius){ GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); d.setStroke(dp(sw),stroke); return d; }
    private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);}
    private void pickMedia(String mime){ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*"); i.putExtra(Intent.EXTRA_MIME_TYPES,mime.split(",")); i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true); startActivityForResult(i,PICK_MEDIA); }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){ super.onActivityResult(requestCode,resultCode,data); if(requestCode==PICK_MEDIA && resultCode==RESULT_OK && data!=null){ if(data.getClipData()!=null){for(int n=0;n<data.getClipData().getItemCount();n++){Uri u=data.getClipData().getItemAt(n).getUri(); getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION); selectedMedia.add(u);}} else if(data.getData()!=null){Uri u=data.getData(); try{getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){} selectedMedia.add(u);} showPage("الاستوديو"); toast("تم اختيار الملفات. الحفظ الدائم وربط النشر قيد التنفيذ."); }}
    private String displayName(Uri uri){String s=uri.getLastPathSegment(); return s==null?uri.toString():s;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
