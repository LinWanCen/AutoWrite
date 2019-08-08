package AutoWrite;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 对齐 XML 属性
 */
public class AttributeAlign extends AbstractModifyFile {

    public static void main(String[] args) {
        new AttributeAlign().execute(args, 1, 2, 3, 4);
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
            if (line.startsWith("<?")
                    || !line.contains("=\"")
                    || !line.contains("/>")
                    || line.matches("^ *<!.*")) {
                list.add(Collections.singletonList(line));
                continue;
            }
            line = line.replaceAll("(<\\w* +)(\\w)", "$1\27$2");
            line = line.replaceAll("(\\w *)(=\")", "$1\27$2");
            line = line.replaceAll("(\" +)(\\w)", "$1\27$2");
            line = line.replaceAll("(\" *)(/>)", "$1\27$2");
            line = line.replaceAll("(.)(-->)", "$1\27$2");
            // if (!line.contains("<")) {
            //     line = line.replaceAll("(^ +)(\\w)", "$1\27$2");
            // }
            String[] split = line.split("\27");
            List<String> ls = Arrays.asList(split);
            System.out.println(" "+ ls);
            list.add(ls);
            for (int i = 0; i < split.length; i++) {
                int length = AlignUtils.displayLen(split[i]);
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
                    w.write(s);
                    if (colLenMax.size() < i + 1) {
                        continue;
                    }
                    int len = colLenMax.get(i) - AlignUtils.displayLen(s);
                    for (int j = 0; j < len; j++) {
                        w.write(" ");
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
