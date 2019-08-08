**Html Crawler**
-
Simple Java application which searches similar xml element in xml-specific files after light attributes/tag changes.

Running application steps:
  -
  1) Run mvn clean package 
  2) Run java -jar target/html-crawler-1.0-SNAPSHOT-jar-with-dependencies.jar
  <input_origin_file_path>
  <input_other_sample_file_path>
  <element_id>
  
  where <input_origin_file_path> - absolute path to origin file
        <input_other_sample_file_path> - absolute path to crawling file
        <element_id> - id of target element (optional, by default "make-everything-ok-button" is used)