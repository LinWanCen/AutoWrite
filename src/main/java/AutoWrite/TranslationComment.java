package AutoWrite;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 双语注释
 */
public class TranslationComment extends AbstractModifyFile {

    public static void main(String[] args) {
        new TranslationComment().execute(args, 1, 2, 3, 4);
    }

    @Override
    protected void readWrite(File file, File tempFile) {
        try {
            Path path = file.toPath();
            byte[] bytes = Files.readAllBytes(path);
            String s = new String(bytes, Charset.forName(charsetName));
            s = s.replaceAll("([a-zA-Z]{2,})\\.([a-zA-Z]{2,})", "$1 $2");
            System.out.println("【原文】\n" + s);

            String encode = URLEncoder.encode(s, charsetName);
            String youdao = youdao(encode, lineSeparator);
            System.out.println("【译文】\n" + youdao + "\n\n【译文结束】");
            Files.write(tempFile.toPath(), youdao.getBytes(charsetName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void modify(BufferedReader r, BufferedWriter w) {}


    public static final String ZH_CN2EN = "ZH_CN2EN";
    public static final String EN2ZH_CN = "EN2ZH_CN";

    public static String youdao(String i, String lineSeparator) {
        return youdao(i, null, lineSeparator);
    }

    private static Pattern youdaoPattern = Pattern.compile("(?<=\"tgt\":\").*?(?=\"})");
    private static Pattern casePattern = Pattern.compile("(?<=[a-z])(?=[A-Z])");

    public static String youdao(String i, String type, String lineSeparator) {
        i = casePattern.matcher(i).replaceAll(" ");
        String s = HttpUtils.get(String.format("http://fanyi.youdao.com/translate?doctype=json&type=%s&i=%s", type, i));
        Matcher matcher = youdaoPattern.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            sb.append(matcher.group()).append(lineSeparator);
        }
        int length = sb.length();
        sb.delete(length - lineSeparator.length(), length);
        return sb.toString();
    }

}
