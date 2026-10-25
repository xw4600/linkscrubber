package com.linkscrubber;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ScrubberTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void scrub(String input, String expected) {
        String actual = Scrubber.scrub(input).url;
        check("scrub(" + input + ")", expected, actual);
    }

    private static void urls(String text, List<String> expected) {
        check("findUrls(" + text + ")", expected.toString(), Scrubber.findUrls(text).toString());
    }

    private static void check(String label, String expected, String actual) {
        if (expected.equals(actual)) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL " + label + "\n     expected: " + expected + "\n     actual:   " + actual);
        }
    }

    public static void main(String[] args) {
        scrub("https://site.com/path?a=1&b=2#frag", "https://site.com/path");
        scrub("https://site.com/path?", "https://site.com/path");
        scrub("https://site.com/path", "https://site.com/path");
        scrub("https://site.com/a#frag", "https://site.com/a#frag");
        scrub("https://site.com/a#x?y", "https://site.com/a#x?y");
        scrub("http://x.com/?a=1", "http://x.com/");
        scrub("https://www.google.com/search?q=hi&utm_source=x", "https://www.google.com/search");
        scrub("https://www.youtube.com/watch?v=dQw4w9WgXcQ&feature=share", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        scrub("https://youtu.be/dQw4w9WgXcQ?t=42&si=abc", "https://youtu.be/dQw4w9WgXcQ?t=42");
        scrub("https://youtube.com/watch?feature=share", "https://youtube.com/watch");
        scrub("https://x.com/user/status/123?s=20&t=abc", "https://x.com/user/status/123?s=20");
        scrub("https://www.amazon.co.uk/gp/product/B08?dp=B08&th=1", "https://www.amazon.co.uk/gp/product/B08?dp=B08");
        scrub("https://www.google.com/maps/place/X?q=coffee&utm=1", "https://www.google.com/maps/place/X?q=coffee");
        scrub("https://www.google.com/search?q=maps", "https://www.google.com/search");
        scrub("https://drive.google.com/file/d/ID/view?usp=sharing", "https://drive.google.com/file/d/ID/view?usp=sharing");
        scrub("https://www.dropbox.com/s/abc/file.pdf?dl=0", "https://www.dropbox.com/s/abc/file.pdf?dl=0");
        scrub("https://zoom.us/j/123?pwd=abc&from=x", "https://zoom.us/j/123?pwd=abc");

        urls("Cool article\nhttps://site.com/a?utm=1.", Arrays.asList("https://site.com/a?utm=1"));
        urls("see www.site.com/page?x=1 now", Arrays.asList("https://www.site.com/page?x=1"));
        urls("https://a.com/?x=1 and https://b.com/?y=2", Arrays.asList("https://a.com/?x=1", "https://b.com/?y=2"));
        urls("(https://a.com/x?y=1)", Arrays.asList("https://a.com/x?y=1"));
        urls("no links here", Collections.<String>emptyList());
        urls("", Collections.<String>emptyList());

        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
