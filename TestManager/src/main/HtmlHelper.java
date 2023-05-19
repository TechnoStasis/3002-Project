package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @authors 22887893 YVES MIGUEL REYES 33.3%
 * @authors 23262446 SRINIKETH KARLAPUDI 33.3%
 * @authors 23468614 CHENG LI 33.3%
 */
public class HtmlHelper {

    /**
     * A way to assign dynamic elements in HTML without futzing about the actual
     * HTML file
     * 
     * Implementation is faintly inspired by JINJA formatting, but it not the full
     * implementation, too much work
     * 
     * @param html
     * @param map
     * @return
     */
    public static final String render(String html, HashMap<String, Object> map) {
        String htmlRender = html;
        if (map != null && !map.isEmpty())
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue().toString();

                htmlRender = htmlRender.replace("{{" + key + "}}", val);
            }
        return htmlRender;
    }

    /**
     * 
     * Reads an html file as a text file, converting it to a String object
     * 
     * @param path
     * @return
     */
    public static final String readHTML(String path) {
        String htmlPage = "";
        StringBuilder contentBuilder = new StringBuilder();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL htmlURL = loader.getResource("html/" + path);
        try {
            BufferedReader in = new BufferedReader(
                    new FileReader(new File(htmlURL.toURI())));
            String str;
            while ((str = in.readLine()) != null) {
                contentBuilder.append(str);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        htmlPage = contentBuilder.toString();
        return htmlPage;
    }

    /**
     * Appends font color tags to a string
     */
    public static final String appendError(String line) {
        return "<FONT COLOR=\"RED\">" + line + "</FONT>";
    }

    public static final String createButton(String name) {
        return "<button type=\"submit\">" + name + "</button>";
    }

    public static final String createSubmitButton(String id, String name) {
        return "<input type=\"submit\" name=\"" + id + "\"" + " value=\"" + name + "\">";
    }

    public static final String paragraphTag(String name) {
        return "<p>" + name + "</p>";
    }

    public static final String boldTag(String name) {
        return "<b>" + name + "</b>";
    }

    public static final String largeTextBoxTag(String content) {
        return "<textarea id=\"answer\" name=\"answer\" rows=\"25\" cols=\"25\" style=\"width: 500px; height: 500px;\">"
                + content + "</textarea>";
    }

    public static Object appendGreen(String line) {
        return "<FONT COLOR=\"GREEN\">" + line + "</FONT>";
    }
}
