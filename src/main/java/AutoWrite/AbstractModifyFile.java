package AutoWrite;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * 在主方法新建自身后运行 execute 方法
 * <br>在args没有设置输入目录时默认 files 文件夹下所有文件，没有设置输出目录时默认替换源文件
 * <br>需重写protected void modify(BufferedReader r, BufferedWriter w) throws Exception {
 */
public abstract class AbstractModifyFile {
    public String charsetName = "UTF-8";
    public String lineSeparator = "\r\n";

    /** 业务逻辑，需重写 */
    protected abstract void modify(BufferedReader r, BufferedWriter w) throws Exception;

    /** 读写逻辑，可重写 */
    protected void readWrite(File file, File tempFile) {
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(file), charsetName);
             BufferedReader r = new BufferedReader(in);
             OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(tempFile), charsetName);
             BufferedWriter w = new BufferedWriter(out)) {
            // 业务逻辑，需重写
            modify(r, w);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 获取换行符 */
    public String getLineSeparator(File file){
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(file), charsetName);
             BufferedReader r = new BufferedReader(in)) {
            char[] cbuf = new char[3072];
            int  len;
            while ((len = r.read(cbuf)) > 0){
                String s = new String(cbuf, 0 , len);
                if (s.contains("\r\n")) {
                    return "\r\n"; // Windows
                } else if (s.contains("\n")) {
                    return "\n"; // Unix
                } else if (s.contains("\r")) {
                    return "\r"; // Mac
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return lineSeparator;
    }

    /** 执行方法 */
    protected void execute(String[] args, int inNum, int charsetNum, int outNum, int subNum) {
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
            File parentFile = inPath.getParentFile();
            inPathLength = parentFile == null ? 0 : parentFile.getAbsolutePath().length();
        } else {
            AbstractModifyFile.listDeep(inFileList, null, false, true, inPath);
        }
        Clipboard clipboard = null;
        File clipFile = null;
        if (inFileList.size() == 0) {
            // 若文件个数为空则从剪切板获取
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipFile = new File("clip.txt");
            Transferable content = clipboard.getContents(null);
            if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try (FileWriter fileWriter = new FileWriter(clipFile)) {
                    String text = (String) content.getTransferData(DataFlavor.stringFlavor);
                    fileWriter.write(text);
                    inFileList.add(clipFile);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            charsetName = System.getProperty("file.encoding");
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

        // region 截取处理
        int startLine = 0;
        int endLine = 0;
        int startColumn = 0;
        int endColumn = 0;
        if (args.length >= subNum) {
            startLine = Integer.parseInt(args[subNum - 1]);
        }
        if (args.length >= subNum + 1) {
            endLine = Integer.parseInt(args[subNum]);
        }
        if (args.length >= subNum + 2) {
            startColumn = Integer.parseInt(args[subNum + 1]);
        }
        if (args.length >= subNum + 3) {
            endColumn = Integer.parseInt(args[subNum + 2]);
        }
        // endregion 截取处理

        // 临时文件以免边读边写
        String parent = inPath.getParent();
        File tempPath = new File(parent, "AutoWriteTemp");
        if (!tempPath.exists()) {
            boolean mkdirs = tempPath.mkdirs();
        }

        for (int i = 0; i < inFileList.size(); i++) {
            File file = inFileList.get(i);
            lineSeparator = getLineSeparator(file);
            System.out.println(String.format("%s/%s files:%s", i + 1, inFileList.size(), file.getName()));
            String child = file.getAbsolutePath().substring(inPathLength);
            File outFile = new File(tempPath, child);
            outFile.getParentFile().mkdirs();
            File inFile = file;
            // region 输入截取处理
            if (startLine > 0 ) {
                inFile = new File("subFile.txt");
                try (InputStreamReader in = new InputStreamReader(new FileInputStream(file), charsetName);
                     BufferedReader r = new BufferedReader(in);
                     OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(inFile), charsetName);
                     BufferedWriter w = new BufferedWriter(out)) {
                    String line;
                    int row = 0;
                    while ((line = r.readLine()) != null) {
                        row++;
                        if (row < startLine) {
                            continue;
                        }
                        if (row == startLine && startLine == endLine) {
                            line = startColumn == 0 ? line : line.substring(startColumn - 1, endColumn - 1);
                        } else if (row == startLine) {
                            line = startColumn == 0 ? line : line.substring(startColumn - 1);
                        } else if (row == endLine) {
                            line = startColumn == 0 ? line : line.substring(0, endColumn - 1);
                        }
                        w.write(line);
                        w.write(lineSeparator);
                        if (row == endLine) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                System.out.println(" input subFile.txt");
            }
            // endregion 输入截取处理

            // 读写逻辑，可重写
            readWrite(inFile, outFile);

            // region 输出截取处理
            if (startLine > 0) {
                try (InputStreamReader in = new InputStreamReader(new FileInputStream(file), charsetName);
                     BufferedReader r = new BufferedReader(in);
                     OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(inFile), charsetName);
                     BufferedWriter w = new BufferedWriter(out)) {
                    byte[] bytes = Files.readAllBytes(outFile.toPath());
                    String s = new String(bytes, charsetName);
                    s = s.replaceFirst(lineSeparator + "$", "");
                    String line;
                    int row = 0;
                    while ((line = r.readLine()) != null) {
                        row++;
                        if (row == startLine && startLine == endLine) {
                            w.write(startColumn == 0 ? "" : line.substring(0, startColumn - 1));
                            w.write(s);
                            w.write(startColumn == 0 ? "" : line.substring(endColumn - 1));
                            w.write(lineSeparator);
                        } else if (row == startLine) {
                            w.write(startColumn == 0 ? "" : line.substring(0, startColumn - 1));
                            w.write(s);
                        } else if (row == endLine) {
                            w.write(startColumn == 0 ? "" : line.substring(endColumn - 1));
                            w.write(lineSeparator);
                        } else if (row < startLine || row > endLine) {
                            w.write(line);
                            w.write(lineSeparator);
                        }
                    }
                    outFile = inFile;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                System.out.println(" output subFile.txt");
            }
            // endregion 输出截取处理

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
            boolean b = outFile.renameTo(file);
            if (b) {
                System.out.println(" " + file.getAbsolutePath());
            } else {
                System.err.println(" " + file.getAbsolutePath());
            }
        }
        tempPath.delete();
        if (clipboard != null) {
            try {
                byte[] bytes = Files.readAllBytes(clipFile.toPath());
                String s = new String(bytes, charsetName);
                StringSelection selection = new StringSelection(s);
                clipboard.setContents(selection, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            clipFile.delete();
        }
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