package AutoWrite;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 连续正则替换
 */
public class ReplaceAllMultiple extends AbstractModifyFile {
    private static final String DISABLE_PREFIX = "//";
    private static final String PARAM_TITLE = "title";
    private static final String PARAM_FOOTER = "footer";
    private static final String PARAM_CASE = "case";
    private static final String PARAM_LOOP = "loop";
    private static final String PARAM_GET = "get";
    private static final int PARAM_OFFSET = 0;
    private static final int REGEXP_OFFSET = 1;
    private static final int REPLACEMENT_OFFSET = 2;
    private static final int REGEX_FILE_LINE = 4;

    private static final ArrayList<ReClass> reClassList = new ArrayList<>();

    private static class ReClass {
        /** 特殊操作参数 */
        private String param;
        /** 是否不生效 */
        private boolean disable;
        /** 是否标题 */
        private boolean title;
        /** 是否末行 */
        private boolean footer;
        /** 是否循环 */
        private boolean loop;
        /** 是否正则获取 */
        private boolean get;

        /** 正则表达式 */
        private String regex;
        /** 正则匹配器 */
        private Pattern pattern;
        /** 正则替换式 */
        private String replacement;
    }

    /**
     * @param args regexFile inFileOrPath outFileOrPath
     * @since 1.7
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("regexCharset and regexFile is null!");
            return;
        }
        // region 正则列表文件
        Path regexFile = Paths.get(args[1]);
        List<String> regexList = Files.readAllLines(regexFile, Charset.forName(args[0]));
        // 去掉前两行
        regexList.remove(0);
        regexList.remove(0);
        int lineNum = regexList.size() % REGEX_FILE_LINE;
        if (lineNum == PARAM_OFFSET) {
            regexList.remove(regexList.size() - 1);
        } else if (lineNum == REGEXP_OFFSET) {
            regexList.remove(regexList.size() - 1);
            regexList.remove(regexList.size() - 1);
        } else if (lineNum == REPLACEMENT_OFFSET) {
            // 少了一行替换后的表达式默认空白
            regexList.add("");
        }
        for (int i = 0; i < regexList.size(); i += REGEX_FILE_LINE) {
            ReClass reClass = new ReClass();
            reClass.param = regexList.get(i + PARAM_OFFSET);
            reClass.disable = reClass.param.startsWith(DISABLE_PREFIX);
            if (reClass.disable) {
                reClassList.add(reClass);
                continue;
            }
            reClass.replacement = unescapeJava(regexList.get(i + REPLACEMENT_OFFSET));
            reClass.title = reClass.param.contains(PARAM_TITLE);
            reClass.footer = reClass.param.contains(PARAM_FOOTER);
            if (reClass.title || reClass.footer) {
                reClassList.add(reClass);
                continue;
            }
            reClass.regex = regexList.get(i + REGEXP_OFFSET);
            int flags = Pattern.MULTILINE;
            if (reClass.param.contains(PARAM_CASE)) {
                flags += Pattern.CASE_INSENSITIVE;
            }
            reClass.pattern = Pattern.compile(reClass.regex, flags);
            reClass.get = reClass.param.contains(PARAM_GET);
            reClass.loop = reClass.param.contains(PARAM_LOOP);
            reClassList.add(reClass);
        }
        // endregion 正则列表文件

        new ReplaceAllMultiple().execute(args, 3, 4, 5, 6);
    }

    @Override
    protected void readWrite(File file, File tempFile) {
        try {
            Path path = file.toPath();
            byte[] bytes = Files.readAllBytes(path);
            String s = new String(bytes, Charset.forName(charsetName));

            // region 【核心】正则替换
            for (int j = 0; j < reClassList.size(); j++) {
                ReClass reClass = reClassList.get(j);
                if (reClass.disable) {
                    System.out.println((String.format(" %s/%s disable", j + 1, reClassList.size())));
                    continue;
                }
                // 添加标题，也可以用下面的正则完成，不过效率太低所以这里添加功能
                // ([\s\S]*)
                // 标题文本\n([\s\S]*)
                if (reClass.title) {
                    s = reClass.replacement + s;
                    System.out.println((String.format(" %s/%s title", j + 1, reClassList.size())));
                    continue;
                }
                // 添加末行
                if (reClass.footer) {
                    s = s + reClass.replacement;
                    System.out.println((String.format(" %s/%s footer", j + 1, reClassList.size())));
                    continue;
                }

                System.out.print(String.format(" %s/%s regex:%s, replacement:%s, count:",
                        j + 1, reClassList.size(), reClass.regex, reClass.replacement));

                // 缓存替换前的字符串
                String temp;
                int count = 1;
                if (reClass.get) {
                    Matcher matcher = reClass.pattern.matcher(s);
                    StringBuilder sb = new StringBuilder();
                    if (matcher.find()) {
                        sb.append(matcher.group());
                    }
                    while (matcher.find()) {
                        sb.append(lineSeparator).append(matcher.group());
                    }
                    s = sb.toString();
                } else {
                    do {
                        temp = s;
                        System.out.print(" " + count++);
                        s = reClass.pattern.matcher(s).replaceAll(reClass.replacement);
                    } while (reClass.loop && !s.equals(temp)); // 若循环，替换到替换前后一致为止
                }
                System.out.println();
            }
            // endregion【核心】正则替换

            bytes = s.getBytes(charsetName);
            Files.write(tempFile.toPath(), bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void modify(BufferedReader r, BufferedWriter w) {}
}