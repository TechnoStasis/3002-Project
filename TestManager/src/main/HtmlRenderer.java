package main;

import java.util.HashMap;
import java.util.Map;

public class HtmlRenderer {

    public static final String render(String html, HashMap<String, Object> map) {
        String htmlRender = html;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue().toString();

            htmlRender = htmlRender.replace("{{" + key + "}}", val);
        }
        return htmlRender;
    }

}
