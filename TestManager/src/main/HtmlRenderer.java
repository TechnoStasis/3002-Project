package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HtmlRenderer {

    public static final String TEMPLATE_PATH = "/Users/yvesreyes/Documents/3002-Project/TestManager/src/main/page/html/";

    /**
     * A way to assign dynamic elements in HTML without futzing about the actual HTML file
     * 
     * Implementation is faintly inspired by JINJA formatting, but it not the full implementation, too much work
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
     * @param path
     * @return
     */
    public static final String readHTML(String path) {
        String htmlPage = "";
        StringBuilder contentBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(
                    new FileReader(TEMPLATE_PATH + path));
            String str;
            while ((str = in.readLine()) != null) {
                contentBuilder.append(str);
            }
            in.close();
        } catch (IOException e) {
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
}
