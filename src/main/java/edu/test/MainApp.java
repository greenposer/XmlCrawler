package edu.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MainApp {

    private static Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    private static String CHARSET_NAME = "utf8";

    public static void main(String[] args) {
        try {
            String originPath = args[0];
            String samplePath = args[1];

            String targetElementId = args.length > 2 ? args[2] : "make-everything-ok-button";
            String result = new XmlCrawler().crawlOutputFile(new File(originPath), new File(samplePath), targetElementId);

            System.out.println(result);
        } catch (Exception e) {
            LOGGER.error("Exception {}", e);
        }
    }
}
