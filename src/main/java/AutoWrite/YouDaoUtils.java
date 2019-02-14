package AutoWrite;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 有道翻译
 */
public class YouDaoUtils {
    public static final String ZH_CN2EN = "ZH_CN2EN";
    public static final String EN2ZH_CN = "EN2ZH_CN";

    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s.replaceAll("\\W+", " ")).append(" ");
        }
        System.out.println("原文：\n" + sb.toString());
        System.out.println("\n译文：\n" + youdao(sb.toString()));
    }

    public static String youdao(String i) {
        return youdao(i, null);
    }

    private static Pattern youdaoPattern = Pattern.compile("(?<=\"tgt\":\").*(?=\"})");
    private static Pattern casePattern = Pattern.compile("(?<=[a-z])(?=[A-Z])");

    public static String youdao(String i, String type) {
        i = casePattern.matcher(i).replaceAll(" ");
        String s = HttpUtils.get(String.format("http://fanyi.youdao.com/translate?doctype=json&type=%s&i=%s", type, i));
        Matcher matcher = youdaoPattern.matcher(s);
        if (matcher.find()) {
            return matcher.group();
        }
        return s;
    }

}
