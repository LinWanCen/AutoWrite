package AutoWrite;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 对齐 markdown 表格
 */
public class MarkdownTableAlign extends AbstractModifyFile {

    public static void main(String[] args) {
        new MarkdownTableAlign().execute(args, 1, 2, 3, 4);
    }

    @Override
    protected void readWrite(File file, File tempFile) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(file.toPath(), Charset.forName(charsetName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 分割后的每一行
        ArrayList<List<String>> list= new ArrayList<>();
        // 每一列最大值
        ArrayList<Integer> colLenMax = new ArrayList<>();
        for (String line : lines) {
            if (!line.contains("|")) {
                list.add(Collections.singletonList(line));
                continue;
            }
            line = line.replaceAll("([^|]+)(\\|)", "$1\27$2");
            String[] split = line.split("\27");
            List<String> ls = Arrays.asList(split);
            System.out.println(" "+ ls);
            list.add(ls);
            for (int i = 0; i < split.length; i++) {
                // 加上占两个空格大小的字符长度
                int length = split[i].length() + split[i].replaceAll("[\\u0000-\\u00FF]","").length();
                if (colLenMax.size() < i+1) {
                    colLenMax.add(length);
                    continue;
                }
                if (colLenMax.get(i) < length) {
                    colLenMax.set(i, length);
                }
            }
        }
        System.out.println(" " + colLenMax);
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), charsetName))){
            for (List<String> ls : list) {
                for (int i = 0; i < ls.size(); i++) {
                    String s = ls.get(i);
                    boolean ishr = s.contains("---");
                    if (ishr) {
                        s = s.replaceAll(" ","-");
                    }
                    w.write(s);
                    if (colLenMax.size() < i + 1) {
                        continue;
                    }
                    int len = colLenMax.get(i) - s.length() - s.replaceAll("[\\u0000-\\u00FF]","").length();
                    for (int j = 0; j < len; j++) {
                        if (ishr) {
                            w.write("-");
                        } else {
                            w.write(" ");
                        }
                    }
                }
                w.write(lineSeparator);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void modify(BufferedReader r, BufferedWriter w) {}
}
