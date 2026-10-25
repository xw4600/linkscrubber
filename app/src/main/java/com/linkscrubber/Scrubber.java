package com.linkscrubber;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Scrubber {

    public static final class Result {
        public final String url;
        public final String note;

        Result(String url, String note) {
            this.url = url;
            this.note = note;
        }
    }

    private static final Pattern URL_PATTERN = Pattern.compile("(?i)(?:https?://|www\\.)[^\\s<>\"']+");
    private static final String TRAILING_JUNK = ".,;:!)]}>";

    private Scrubber() {
    }

    public static List<String> findUrls(String text) {
        List<String> urls = new ArrayList<>();
        if (text == null) {
            return urls;
        }
        Matcher m = URL_PATTERN.matcher(text);
        while (m.find()) {
            String url = trimTrailing(m.group());
            if (url.regionMatches(true, 0, "www.", 0, 4)) {
                url = "https://" + url;
            }
            if (!urls.contains(url)) {
                urls.add(url);
            }
        }
        return urls;
    }

    public static Result scrub(String url) {
        int q = url.indexOf('?');
        int h = url.indexOf('#');
        if (q < 0 || (h >= 0 && h < q)) {
            return new Result(url, null);
        }

        String base = url.substring(0, q);
        String rest = url.substring(q + 1);
        String query = rest;
        String fragment = "";
        int rh = rest.indexOf('#');
        if (rh >= 0) {
            query = rest.substring(0, rh);
            fragment = rest.substring(rh);
        }

        ScrubRules.Rule rule = ScrubRules.match(base);
        if (rule == null) {
            return new Result(base, "Removed query string");
        }

        Set<String> kept = keptKeys(query, rule.keep);
        if (kept.isEmpty()) {
            return new Result(base, "Removed query string (" + rule.name + " rule)");
        }
        String keptQuery = keepParams(query, rule.keep);
        return new Result(base + "?" + keptQuery + fragment,
                "Kept " + String.join(", ", kept) + " (" + rule.name + " rule)");
    }

    private static String trimTrailing(String url) {
        int end = url.length();
        while (end > 0 && TRAILING_JUNK.indexOf(url.charAt(end - 1)) >= 0) {
            end--;
        }
        return url.substring(0, end);
    }

    private static String keepParams(String query, Set<String> keep) {
        StringBuilder out = new StringBuilder();
        for (String pair : query.split("&")) {
            if (keyOf(pair).isEmpty() || !keep.contains(keyOf(pair))) {
                continue;
            }
            if (out.length() > 0) {
                out.append('&');
            }
            out.append(pair);
        }
        return out.toString();
    }

    private static Set<String> keptKeys(String query, Set<String> keep) {
        Set<String> keys = new LinkedHashSet<>();
        for (String pair : query.split("&")) {
            String key = keyOf(pair);
            if (!key.isEmpty() && keep.contains(key)) {
                keys.add(key + "=");
            }
        }
        return keys;
    }

    private static String keyOf(String pair) {
        int eq = pair.indexOf('=');
        return eq < 0 ? pair : pair.substring(0, eq);
    }
}
