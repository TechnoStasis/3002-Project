package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HtmlRenderer {

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

    public static final String readHTML(String path) {
        String htmlPage = "";
        StringBuilder contentBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(
                    new FileReader(TestManager.TEMPLATE_PATH + path));
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

    public static final String appendError(String line)
    {
        return "<FONT COLOR=\"RED\">" + line + "</FONT>";
    }
}
