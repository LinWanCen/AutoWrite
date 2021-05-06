package AutoWrite;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * <br>打印file:///链接点击不会在浏览器中打开，所以不使用
 */
public class JavaDocHtml {

    public static void main(String[] args) throws Exception {
        String refreshMode = "0";
        if (args.length == 0) {
            System.err.println("please run with program arguments: jarFile [refreshMode]");
            System.err.println("refreshMode 0-不重新解压 1-重新解压单个 2-重新解压所有 3-删除全部并重新解压所有");
            return;
        }
        if (args.length > 1) {
            refreshMode = args[1];
        }
        unZipAndOpen(args[0], refreshMode);
    }

    /**
     * 解析传入的路径并打开
     * <br>"....jar"!.class"
     *
     * @param refreshMode 若 HTML 文件夹已存在 0-不重新解压 1-重新解压单个 2-重新解压所有 3-删除全部并重新解压所有
     */
    public static void unZipAndOpen(String path, String refreshMode) throws Exception {
        String[] split = path.split("[\"!]++");
        String javaDocJarName;
        if (split[0].endsWith("-javadoc.jar")) {
            javaDocJarName = split[0];
        } else if (split[0].endsWith("-sources.jar")) {
            String jarNameNotExtension = split[0].substring(0, split[0].length() - 12);
            javaDocJarName = jarNameNotExtension + "-javadoc.jar";
        } else if (split[0].endsWith(".jar")) {
            String jarNameNotExtension = split[0].substring(0, split[0].length() - 4);
            javaDocJarName = jarNameNotExtension + "-javadoc.jar";
        } else {
            System.err.println("not .jar or -javadoc.jar: " + split[0]);
            return;
        }
        File inFile = new File(javaDocJarName);
        if (!inFile.exists()) {
            System.err.println("javadoc not exists: " + inFile.getAbsolutePath());
            System.err.println("your can use:");
            System.err.println("mvn dependency:get -Dartifact=g:a:v:jar:javadoc");
            return;
        }
        File outDir = new File(javaDocJarName.substring(0, javaDocJarName.length() - 4));
        String classHtmlName = null;
        if (split.length > 1 && split[1].endsWith(".class")) {
            String classNameNotExtension = split[1].substring(0, split[1].length() - 6);
            classHtmlName = classNameNotExtension + ".html";
        }
        if (!outDir.exists()) {
            System.out.println("begin unZipAll: \"" + inFile + "\" \"" + outDir +"\"");
            unZipAll(inFile, outDir);
        } else if (classHtmlName != null) {
            if ("3".equals(refreshMode)) {
                System.out.println("begin deleteDir: \"" + outDir +"\"");
                deleteDir(outDir);
                System.out.println("begin unZipAll: \"" + inFile + "\" \"" + outDir +"\"");
                unZipAll(inFile, outDir);
            } else if ("2".equals(refreshMode)) {
                System.out.println("begin unZipAll: \"" + inFile + "\" \"" + outDir +"\"");
                unZipAll(inFile, outDir);
            } else if ("1".equals(refreshMode)) {
                System.out.println("begin unZipOne: \"" + inFile + "!" + classHtmlName  + "\" \"" + outDir +"\"");
                unZipOne(inFile, outDir, classHtmlName);
            }
        }
        File htmlFile = null;
        if (classHtmlName != null) {
            File classHtmlFile = new File(outDir, classHtmlName);
            if (classHtmlFile.exists()) {
                htmlFile = classHtmlFile;
            } else {
                System.err.println("classHtmlFile not exists: " + classHtmlFile);
            }
        }
        if (htmlFile == null) {
            htmlFile = new File(outDir, "index.html");
            if (!htmlFile.exists()) {
                System.err.println("index.html not exists: " + htmlFile);
                return;
            }
        }
        String command = "cmd.exe /c \"" + htmlFile.getAbsolutePath() + "\"";
        System.out.println(command);
        Runtime.getRuntime().exec(command);
    }

    public static void unZipOne(File inFile, File outDir, String name) throws Exception {
        name = name.replace('\\', '/');
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            ZipFile zipFile = new ZipFile(inFile);
            ZipEntry entry = zipFile.getEntry(name);
            if (entry == null) {
                System.err.println("entry not exists: " + name);
            }
            inputStream = zipFile.getInputStream(entry);
            File outFile = new File(outDir, name);
            outFile.getParentFile().mkdirs();
            outputStream = new FileOutputStream(outFile);
            byte[] buf1 = new byte[1024];
            int len;
            while ((len = inputStream.read(buf1)) > 0) {
                outputStream.write(buf1, 0, len);
            }
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static void unZipAll(File inFile, File outDir) throws Exception {
        outDir.mkdirs();
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(inFile));
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File outFile = new File(outDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(outFile);
                    byte[] buf1 = new byte[1024];
                    int len;
                    while ((len = zipInputStream.read(buf1)) > 0) {
                        outputStream.write(buf1, 0, len);
                    }
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    zipInputStream.closeEntry(); // TODO
                }
            }
        } finally {
            if (zipInputStream != null) {
                zipInputStream.close();
            }
        }
    }

    private static void deleteDir(File outDir) {
        if (outDir.isDirectory()) {
            File[] files = outDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDir(file);
                }
            }
        }
        outDir.delete();
    }
}
