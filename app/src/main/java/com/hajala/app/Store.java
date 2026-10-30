package com.hajala.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Local persistence for MangaVerse works and the user's custom categories (SharedPreferences + JSON). */
final class Store {
    static final String MANGA = "manga";
    static final String MANHWA = "manhwa";
    static final String ONESHOT = "oneshot";
    static final String PUBLISHED = "published";
    static final String DRAFT = "draft";

    private static final List<String> BUILT_IN_GENRES =
            Arrays.asList("أكشن", "رومانسي", "فانتازيا", "دراما", "كوميديا");

    static final class Work {
        String id = "";
        String title = "";
        String type = MANGA;
        String desc = "";
        String status = DRAFT;
        String cover = "";
        long createdAt;
        final List<String> genres = new ArrayList<>();
    }

    private static final String KEY_WORKS = "works";
    private static final String KEY_GENRES = "custom_genres";

    private final SharedPreferences sp;

    Store(Context c) {
        sp = c.getApplicationContext().getSharedPreferences("mangaverse", Context.MODE_PRIVATE);
    }

    static String typeLabel(String type) {
        if (MANHWA.equals(type)) return "مانهوا";
        if (ONESHOT.equals(type)) return "ونشوت";
        return "مانغا";
    }

    static String statusLabel(String status) {
        return PUBLISHED.equals(status) ? "منشور" : "مسودة";
    }

    // ---------------------------------------------------------------- works

    List<Work> works() {
        List<Work> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(sp.getString(KEY_WORKS, "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                Work w = new Work();
                w.id = o.optString("id");
                w.title = o.optString("title");
                w.type = o.optString("type", MANGA);
                w.desc = o.optString("desc");
                w.status = o.optString("status", DRAFT);
                w.cover = o.optString("cover");
                w.createdAt = o.optLong("createdAt");
                JSONArray g = o.optJSONArray("genres");
                if (g != null) {
                    for (int j = 0; j < g.length(); j++) w.genres.add(g.optString(j));
                }
                out.add(w);
            }
        } catch (JSONException ignored) {
            // corrupted data: start from an empty list
        }
        return out;
    }

    private void saveWorks(List<Work> list) {
        JSONArray a = new JSONArray();
        for (Work w : list) {
            try {
                JSONObject o = new JSONObject();
                o.put("id", w.id);
                o.put("title", w.title);
                o.put("type", w.type);
                o.put("desc", w.desc);
                o.put("status", w.status);
                o.put("cover", w.cover);
                o.put("createdAt", w.createdAt);
                JSONArray g = new JSONArray();
                for (String s : w.genres) g.put(s);
                o.put("genres", g);
                a.put(o);
            } catch (JSONException ignored) {
                // skip a work that cannot be serialised
            }
        }
        sp.edit().putString(KEY_WORKS, a.toString()).apply();
    }

    void addWork(Work w) {
        List<Work> l = works();
        l.add(0, w);
        saveWorks(l);
    }

    void deleteWork(String id) {
        List<Work> l = works();
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).id.equals(id)) {
                l.remove(i);
                break;
            }
        }
        saveWorks(l);
    }

    void setStatus(String id, String status) {
        List<Work> l = works();
        for (Work w : l) {
            if (w.id.equals(id)) w.status = status;
        }
        saveWorks(l);
    }

    // ---------------------------------------------------------------- categories

    static List<String> builtInGenres() {
        return BUILT_IN_GENRES;
    }

    List<String> customGenres() {
        List<String> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(sp.getString(KEY_GENRES, "[]"));
            for (int i = 0; i < a.length(); i++) {
                String s = a.optString(i);
                if (!s.isEmpty()) out.add(s);
            }
        } catch (JSONException ignored) {
            // corrupted data: no custom categories
        }
        return out;
    }

    /** True when [name] (ignoring case and extra spaces) already exists among built-in or custom categories. */
    boolean hasGenre(String name) {
        String key = normalize(name);
        for (String s : BUILT_IN_GENRES) if (normalize(s).equals(key)) return true;
        for (String s : customGenres()) if (normalize(s).equals(key)) return true;
        return false;
    }

    /** Adds a custom category. Returns false if it is empty or already exists. */
    boolean addGenre(String name) {
        String clean = name.trim().replaceAll("\\s+", " ");
        if (clean.isEmpty() || hasGenre(clean)) return false;
        List<String> l = customGenres();
        l.add(clean);
        saveGenres(l);
        return true;
    }

    void removeGenre(String name) {
        List<String> l = customGenres();
        l.remove(name);
        saveGenres(l);
    }

    private void saveGenres(List<String> l) {
        JSONArray a = new JSONArray();
        for (String s : l) a.put(s);
        sp.edit().putString(KEY_GENRES, a.toString()).apply();
    }

    private static String normalize(String s) {
        return s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
