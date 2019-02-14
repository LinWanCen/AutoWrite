package AutoWrite;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Get Post 工具类
 */
public class HttpUtils {

    public static String get(String url) {
        return link(url, null, "GET");
    }

    public static String post(String url, String input) {
        return link(url, input, "POST");
    }

    public static String link(String url, String input, String method) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);

            connection.setRequestMethod(method);
            // connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            connection.connect();

            if (input != null) {
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                out.append(input);
                out.flush();
                out.close();
            }

            InputStream respond = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(respond, "UTF-8"));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            connection.disconnect();
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

}
