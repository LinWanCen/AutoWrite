package cn.wc.AutoWrite;

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * 把行末注释变为doc注释
 */
public class POJODoc extends BaseModifyFile {

    public static void main(String[] args) {
        new POJODoc().execute();
    }

    @Override
    protected void modify(BufferedReader r, BufferedWriter w)throws Exception {
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = r.readLine()) != null) {
            if (line.contains("//")) {
                String left = line.replaceFirst("\\w.*", "");
                String[] split = line.split("//");
                // 有测试表明耗时：sb.delete(0, sb.length()); < sb.setLength(0); < new StringBuilder();
                sb.delete(0, sb.length());
                sb.append(left)
                        .append("/** ")
                        .append(split[1].trim())
                        .append(" */")
                        .append('\n');
                sb.append(left).append(split[0].trim());
                line = sb.toString();
            }
            w.write(line);
            w.newLine();
        }
    }

}
