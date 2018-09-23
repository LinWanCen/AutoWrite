package AutoWrite;

import java.io.*;
import java.util.ArrayList;

/**
 * 在主方法新建自身后运行execute(args, inNum, charsetNum, outNum)方法
 * <br>在args没有设置输入目录时默认 files 文件夹下所有文件，没有设置输出目录时默认替换源文件
 * <br>需重写protected void modify(BufferedReader r, BufferedWriter w) throws Exception {
 */
public abstract class BaseModifyFile {
    public static String charsetName = "UTF-8";

    /** 业务逻辑，需重写 */
    protected abstract void modify(BufferedReader r, BufferedWriter w) throws Exception;

    /** 读写逻辑，可重写 */
    protected void readWrite(File file, File tempFile) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
             BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), charsetName))) {
            // 业务逻辑，需重写
            modify(r, w);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 执行方法 */
    protected void execute(String[] args, int inNum, int charsetNum, int outNum) {
        // 如果设置了编码
        if (args.length >= charsetNum) {
            charsetName = args[charsetNum - 1];
        }

        // region 输入路径
        String[] inFilesName = {"files"};
        // 如果设置了输入文件或路径
        if (args.length >= inNum) {
            inFilesName[0] = args[inNum - 1];
        }
        File inPath = new File(inFilesName[0]);
        // 输入路径或文件不存在则退出
        if (!inPath.exists()) {
            System.err.println(inPath.getAbsolutePath() + " is not exists");
            return;
        }
        ArrayList<File> inFileList = new ArrayList<>();
        int inPathLength = inPath.getAbsolutePath().length();
        if (inPath.isFile()) {
            inFileList.add(inPath);
            inPathLength = inPath.getParentFile().getAbsolutePath().length();
        } else {
            BaseModifyFile.listDeep(inFileList, null, false, true, inPath);
        }
        // endregion 输入路径

        // region 输出路径
        // 默认修改源文件
        String outFileName = null;
        // 如果设置了输出路径
        if (args.length >= outNum) {
            outFileName = args[outNum - 1];
        }
        // 输入为目录或者输出以 / \ 结尾则创建目录
        boolean outIsDirectory = false;
        if (outFileName != null) {
            outIsDirectory = inPath.isDirectory() || outFileName.endsWith("/") || outFileName.endsWith("\\");
            File outFile = new File(outFileName);
            if (!outFile.exists() && outIsDirectory) {
                boolean mkdirs = outFile.mkdirs();
                System.out.println("outFile is not exists, mkdirs " + (mkdirs ? "success" : "fail"));
            } else if (outFile.isDirectory()) {
                outIsDirectory = true;
            }
        }
        // endregion 输出路径

        // 临时文件以免边读边写
        String parent = inPath.getParent();
        File tempPath = new File(parent, "AutoWriteTemp");
        if (!tempPath.exists()) {
            boolean mkdirs = tempPath.mkdirs();
        }

        for (int i = 0; i < inFileList.size(); i++) {
            File file = inFileList.get(i);
            System.out.println(String.format("%s/%s files:%s", i + 1, inFileList.size(), file.getName()));
            String child = file.getAbsolutePath().substring(inPathLength);
            File tempFile = new File(tempPath, child);
            tempFile.getParentFile().mkdirs();
            // 读写逻辑，可重写
            readWrite(file, tempFile);

            // 输出路径处理：如果不是默认修改源文件(使用原 files)
            if (outFileName != null) {
                if (!outIsDirectory) {
                    // 单个文件且设置输出路径时
                    file = new File(outFileName);
                } else {
                    // 多个文件且设置输出路径时
                    file = new File(outFileName, child);
                }
            }
            if (file.exists()) file.delete();
            boolean b = tempFile.renameTo(file);
            if (b) {
                System.out.println(" " + file.getAbsolutePath());
            } else {
                System.err.println(" " + file.getAbsolutePath());
            }
        }
        boolean delete = tempPath.delete();
    }

    /**
     * 转义字符解码
     * org.apache.commons.lang.StringEscapeUtils
     */
    public static String unescapeJava(String str) {
        StringWriter out = new StringWriter(str.length());
        StringBuilder unicode = new StringBuilder();
        int sz = str.length();
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            if (inUnicode) {
                // if files unicode, then we're reading unicode
                // values files somehow
                unicode.append(ch);
                if (unicode.length() == 4) {
                    // unicode now contains the four hex digits
                    // which represents our unicode character
                    try {
                        int value = Integer.parseInt(unicode.toString(), 16);
                        out.write((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;
                    } catch (NumberFormatException nfe) {
                        throw nfe;
                    }
                }
                continue;
            }
            if (hadSlash) {
                // handle an escaped value
                hadSlash = false;
                switch (ch) {
                    case '\\':
                        out.write('\\');
                        break;
                    case '\'':
                        out.write('\'');
                        break;
                    case '\"':
                        out.write('"');
                        break;
                    case 'r':
                        out.write('\r');
                        break;
                    case 'f':
                        out.write('\f');
                        break;
                    case 't':
                        out.write('\t');
                        break;
                    case 'n':
                        out.write('\n');
                        break;
                    case 'b':
                        out.write('\b');
                        break;
                    case 'u': {
                        // uh-oh, we're files unicode country....
                        inUnicode = true;
                        break;
                    }
                    default:
                        out.write(ch);
                        break;
                }
                continue;
            } else if (ch == '\\') {
                hadSlash = true;
                continue;
            }
            out.write(ch);
        }
        if (hadSlash) {
            // then we're files the weird case of a \ at the end of the
            // string, let's output it anyway.
            out.write('\\');
        }
        return out.toString();
    }

    /**
     * 递归子文件夹类的文件夹或文件存入集合
     *
     * @param fileList 要存入的集合
     */
    public static void listDeep(ArrayList<File> fileList, FileFilter filter, boolean dir, boolean file, File... files) {
        for (File f : files) {
            if (f.isDirectory()) {
                if (dir) {
                    fileList.add(f);
                }
                listDeep(fileList, filter, dir, file, f.listFiles(filter));
            } else if (file) {
                fileList.add(f);
            }
        }
    }
}

/**
 * 【文件拓展名】过滤器，忽略大小写
 *
 * @author linWanCheng
 */
class NameEnd implements FileFilter {

    private String[] nameEnds;

    public NameEnd(final String... nameEnds) {
        this.nameEnds = nameEnds;
    }

    @Override
    public boolean accept(final File pathname) {
        if (pathname.isDirectory()) {
            return true;
        }

        String name = pathname.getName();
        int index = name.lastIndexOf(".") + 1;
        String en = index == 0 ? "" : name.substring(index);

        for (String e : nameEnds) {
            if (en.equalsIgnoreCase(e)) {
                return true;
            }

        }
        return false;
    }
}