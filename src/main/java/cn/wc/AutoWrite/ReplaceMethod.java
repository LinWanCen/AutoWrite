package cn.wc.AutoWrite;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 把重写方法的return null;替换为带方法本身的代码
 * 例如：
 * <pre>
 * public String abc(String key) {
 *     return abc(key));
 * }
 * </pre>
 */
public class ReplaceMethod extends BaseModifyFile {

    public static void main(String[] args) {
        new ReplaceMethod().execute();
    }

    private static final String METHOD_REGEX = ".* .*\\(.*\\).*\\{";
    private static final Pattern ARGS_PATTERN = Pattern.compile("(?<= )(\\w*)(?=[^\\w \\\\.])");
    private static final String REPLACE_STRING = "return null;";

    private static final String START_STRING = "return excute(jedis -> jedis.";
    private static final String END_STRING = ");";

    @Override
    protected void modify(BufferedReader r, BufferedWriter w) throws Exception {
        String line;
        String method = null;
        while ((line = r.readLine()) != null) {
            // 去掉左右多余空格
            String s = line.trim();
            if (s.matches(METHOD_REGEX)) {
                // @Override
                // public String set(String key, String value) {
                //     return null;
                // }
                int i1 = s.indexOf("(");
                int i2 = s.indexOf(") {");
                String[] split = s.substring(0, i1).split(" ");
                StringBuilder sb = new StringBuilder(START_STRING);
                sb.append(split[split.length - 1]);

                sb.append("(");
                // 包括右括号用于匹配，替换掉<>
                String as = s.substring(i1, i2 + 1).replaceAll("<.*>", "");
                s.contains("<");
                Matcher matcher = ARGS_PATTERN.matcher(as);
                boolean d = false;
                while (matcher.find()) {
                    if (d) {
                        sb.append(", ");
                    } else {
                        d = true;
                    }
                    String group = matcher.group();
                    sb.append(group);
                }
                sb.append(")");

                sb.append(END_STRING);
                method = sb.toString();
            }
            if (method != null && s.contains(REPLACE_STRING)) {
                line = line.replace(REPLACE_STRING, method);
                System.out.println(method);
            }
            w.write(line);
            w.newLine();
        }
    }
}
