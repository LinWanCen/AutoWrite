package cn.wc.AutoWrite;

import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 为get/set方法添加字段上的文档注释
 */
public class GetSetDoc extends BaseModifyFile {

    public static void main(String[] args) {
        new GetSetDoc().execute();
    }

    private static final String FIELD_REGEX = "[\\w ]* \\w*;";
    private static final Pattern FIELD_PATTERN = Pattern.compile("(?<= )(\\w*)(?=[;])");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(?<=et)(\\w*)(?=[\\(])");

    /** GetSet标志与前缀 */
    enum GetSetEnum{
        /** 获取 */ GET("获取"),
        /** 设置 */ SET("设置");
        String prefix;
        GetSetEnum(String prefix) {
            this.prefix = prefix;
        }
    }

    @Override
    protected void modify(BufferedReader r, BufferedWriter w) throws Exception {
        w.newLine();
        String line;
        // GetSet标志
        GetSetEnum getSetFlag = null;
        StringBuilder sb = new StringBuilder();
        StringBuilder fieldSb = new StringBuilder();
        boolean docFlag = false;
        StringBuilder docSb = new StringBuilder();
        HashMap<String, String> fieldMap = new HashMap<>();
        while ((line = r.readLine()) != null) {
            // 保存doc注释
            int a = line.indexOf("/**");
            int c = line.indexOf("*/");
            if (a >= 0) {
                docFlag = true;
            }
            if (docFlag){
                if (c < 0) {
                    docSb.append(line.substring(a < 0 ? 0 : a + 3).trim()).append('\n');
                } else {
                    docSb.append(line.substring(a < 0 ? 0 : a + 3, c).trim());
                    docFlag = false;
                }
            } else {
                if (!"".equals(line.trim())){
                    // 添加doc注释
                    if (!line.contains("return") && line.matches(FIELD_REGEX)) {
                        Matcher fileMatcher = FIELD_PATTERN.matcher(line);
                        if (fileMatcher.find()) {
                            fieldMap.put(fileMatcher.group(), docSb.toString());
                        }
                    }

                    // 过了有内容的行后doc失效
                    docSb.delete(0, docSb.length());
                }

                // 标记GetSet
                if (line.contains("get")) {
                    getSetFlag = GetSetEnum.GET;
                }
                if (line.contains("set")) {
                    getSetFlag = GetSetEnum.SET;
                }
                if (getSetFlag != null) {
                    String left = line.replaceFirst("\\w.*", "");
                    Matcher filedNameMatcher = METHOD_PATTERN.matcher(line);
                    // 重置 sb
                    sb.delete(0, sb.length());
                    sb.append(left)
                            .append("/** ")
                            .append(getSetFlag.prefix);

                    // 添加对应文档注释
                    if (filedNameMatcher.find()) {
                        String group = filedNameMatcher.group();
                        // 重置 fieldSb
                        fieldSb.delete(0, fieldSb.length());
                        // 首字母小写
                        fieldSb.append(Character.toLowerCase(group.charAt(0)));
                        fieldSb.append(group.substring(1));
                        // 查找对应注释
                        String fieldDoc = fieldMap.get(fieldSb.toString());
                        if (fieldDoc != null) {
                            sb.append(fieldDoc);
                        }
                    }
                    sb.append(" */").append('\n');

                    sb.append(line);
                    line = sb.toString();
                }
                // 重置标记
                getSetFlag = null;
            }

            w.write(line);
            w.newLine();
        }
    }

}
