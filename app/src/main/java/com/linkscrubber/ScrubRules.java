package com.linkscrubber;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Sites whose query string carries identity. For these hosts only the listed
 * parameters are kept; everything else in the query is dropped.
 * Add a site by appending one rule: host regex (without the subdomain part),
 * optional path prefix, and the parameter names to keep.
 */
final class ScrubRules {

    static final class Rule {
        final String name;
        final Pattern host;
        final String pathPrefix;
        final Set<String> keep;

        Rule(String name, String hostRegex, String pathPrefix, String... keep) {
            this.name = name;
            this.host = Pattern.compile("(?:.*\\.)?" + hostRegex);
            this.pathPrefix = pathPrefix;
            this.keep = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(keep)));
        }
    }

    private static final Rule[] RULES = {
            new Rule("youtube.com", "youtube\\.com", "", "v", "t", "list"),
            new Rule("youtu.be", "youtu\\.be", "", "v", "t", "list"),
            new Rule("x.com", "x\\.com", "", "s"),
            new Rule("twitter.com", "twitter\\.com", "", "s"),
            new Rule("amazon", "amazon\\.(?:[a-z]{2,3}\\.)?[a-z]{2,3}", "", "dp"),
            new Rule("google maps", "google\\.(?:[a-z]{2,3}\\.)?[a-z]{2,3}", "/maps", "q", "place_id"),
            new Rule("maps.google.com", "maps\\.google\\.(?:[a-z]{2,3}\\.)?[a-z]{2,3}", "", "q", "place_id"),
            new Rule("drive.google.com", "drive\\.google\\.(?:[a-z]{2,3}\\.)?[a-z]{2,3}", "", "usp"),
            new Rule("dropbox.com", "dropbox\\.com", "", "dl"),
            new Rule("zoom.us", "zoom\\.us", "", "pwd"),
    };

    private ScrubRules() {
    }

    static Rule match(String base) {
        int scheme = base.indexOf("://");
        if (scheme < 0) {
            return null;
        }
        String afterScheme = base.substring(scheme + 3);
        int slash = afterScheme.indexOf('/');
        String host = slash < 0 ? afterScheme : afterScheme.substring(0, slash);
        String path = slash < 0 ? "" : afterScheme.substring(slash);
        host = host.toLowerCase(Locale.ROOT);
        int at = host.lastIndexOf('@');
        if (at >= 0) {
            host = host.substring(at + 1);
        }
        int colon = host.indexOf(':');
        if (colon >= 0) {
            host = host.substring(0, colon);
        }
        for (Rule rule : RULES) {
            if (rule.host.matcher(host).matches() && path.startsWith(rule.pathPrefix)) {
                return rule;
            }
        }
        return null;
    }
}
