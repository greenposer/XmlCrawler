package edu.test;

import lombok.Builder;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class XmlCrawler {

    public static final String HREF = "href";
    private static Logger LOGGER = LoggerFactory.getLogger(XmlCrawler.class);

    private static String CHARSET_NAME = "utf8";

    public String crawlOutputFile(File originFile, File crawlingFile, String elementId) {
        try {
            Document originDoc = Jsoup.parse(
                    originFile,
                    CHARSET_NAME,
                    originFile.getAbsolutePath());

            Document crawlingDoc = Jsoup.parse(
                    crawlingFile,
                    CHARSET_NAME,
                    crawlingFile.getAbsolutePath());

            Element elementById = findElementById(originDoc, elementId)
                    .orElseThrow(() -> new IllegalArgumentException("No elements by provided Id present"));

            return processCrawlingDocument(elementById, crawlingDoc);

        } catch (Exception e) {
            LOGGER.error("Exception while crawling", e);
            return null;
        }
    }

    private String processCrawlingDocument(Element element, Document crawlingDoc) {
        Map<String, Set<String>> collect = element.attributes().asList().stream()
                .collect(Collectors.toMap(Attribute::getKey, this::splitAttributes));

        OriginElementInfo build = OriginElementInfo.builder().tag(element.tagName())
                .attributes(collect)
                .build();

        Map<String, Set<Element>> findingsByMatchingAttributes = new HashMap<>();
        for (Map.Entry<String, Set<String>> attributeEntry : build.getAttributes().entrySet()) {
            String attributeName = attributeEntry.getKey();
            Set<Element> elements = attributeEntry.getValue().stream().map(s -> crawlingDoc.getElementsByAttributeValueContaining(attributeName, s))
                    .flatMap(Collection::stream)
                    .filter(element1 -> isElementHidden(element))
                    .collect(Collectors.toSet());

            findingsByMatchingAttributes.put(attributeName, elements);
        }

        Set<Element> matchedElements = findingsByMatchingAttributes.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toSet());

        Map<Element, Integer> matches = new HashMap<>();
        for (Element matchedElement : matchedElements) {
            findingsByMatchingAttributes.forEach((key, value) -> {
                if (value.contains(matchedElement)) {
                    matches.computeIfPresent(matchedElement, (element1, counter) -> ++counter);
                    matches.putIfAbsent(matchedElement, 1);
                }
            });
        }

        return processMatchesResult(build, matches);
    }

    private String processMatchesResult(OriginElementInfo build, Map<Element, Integer> matches) {
        if (matches.isEmpty()) {
            LOGGER.error("Unfortunately element is not found");
            return null;
        }

        int max = matches.values().stream()
                .mapToInt(integer -> integer)
                .max()
                .orElseThrow(IllegalArgumentException::new);

        List<Element> collect = matches
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(max))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (collect.size() > 1) {
            List<Element> matchByTag = collect.stream()
                    .filter(element -> element.tagName().equals(build.tag))
                    .collect(Collectors.toList());

            if (matchByTag.size() != 1) {
                LOGGER.info("Suddenly {}} elements are found", collect.size());
                return collect.stream()
                        .map(this::makeResultString)
                        .collect(Collectors.joining("\n"));
            }
        }

        return makeResultString(collect.stream().findAny().orElseThrow(IllegalArgumentException::new));
    }

    private String makeResultString(Element element) {
        List<Element> parents = IntStream.range(0, element.parents().size())
                .mapToObj(i -> element.parents().get(element.parents().size() - i - 1))
                .collect(Collectors.toList());

        StringBuffer stringBuffer = new StringBuffer();
        int elementNestingCounter = 0;
        String previousParent = "";
        for (Element parent : parents) {
            if (previousParent.equals(parent.tagName())) {
                elementNestingCounter++;
            } else {
                elementNestingCounter = 0;
            }

            if (elementNestingCounter > 0) {
                stringBuffer.append(parent.tagName()).append("[").append(elementNestingCounter).append("] > ");
            } else {
                stringBuffer.append(parent.tagName() + " > ");
            }

            previousParent = parent.tagName();
        }

        stringBuffer.append(element.tagName());
        return stringBuffer.toString();
    }

    private boolean isElementHidden(Element element) {
        if (!element.hasAttr("style")) {
            return true;
        } else {
            return !element.attributes().get("style").equals("display:none");
        }
    }

    private HashSet<String> splitAttributes(Attribute attribute) {
        String value = attribute.getValue();

        HashSet<String> result = Stream.of(attribute.getValue().split(" "))
                .flatMap(s -> Stream.of(s.split("-")))
                .map(s -> {
                    if (attribute.getKey().equals(HREF)) {
                        return attribute.getValue().replace("#", "");
                    }

                    return attribute.getValue();
                }).collect(Collectors.toCollection(HashSet::new));
        result.add(value);

        return result;
    }

    @Builder
    @Data
    private static class OriginElementInfo {
        private String tag;
        private Map<String, Set<String>> attributes;
    }

    public Optional<Element> findElementById(Document doc, String targetElementId) {
        return Optional.of(doc.getElementById(targetElementId));
    }
}
