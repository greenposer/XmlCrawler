package edu.test;

import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class XmlCrawlerTest {

    public static final String ELEMENT_ID = "make-everything-ok-button";
    public static final String ORIGIN_FILE = "sample/input-origin.html";
    public static final List<String> SAMPLES = Stream.of("sample/sample-1-evil-gemini.html", "sample/sample-2-container-and-clone.html",
            "sample/sample-3-the-escape.html", "sample/sample-4-the-mash.html").collect(Collectors.toList());

    public static final List<String> EXPECTED_TESULTS = Stream.of("html > body > div > div[1] > div[2] > div[3] > div[4] > div[5] > a",
            "html > body > div > div[1] > div[2] > div[3] > div[4] > div[5] > div[6] > a",
            "html > body > div > div[1] > div[2] > div[3] > div[4] > div[5] > a",
            "html > body > div > div[1] > div[2] > div[3] > div[4] > div[5] > a")
            .collect(Collectors.toList());

    private XmlCrawler xmlCrawler = new XmlCrawler();

    @Test
    public void testCrawlHtml() {
        for (int i = 0; i < SAMPLES.size(); i++) {
            String result = new XmlCrawler().crawlOutputFile(new File(ORIGIN_FILE), new File(SAMPLES.get(i)), ELEMENT_ID);
            assertEquals(EXPECTED_TESULTS.get(i), result);
        }
    }
}