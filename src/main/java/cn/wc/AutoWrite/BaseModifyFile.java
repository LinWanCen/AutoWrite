package cn.wc.AutoWrite;

import java.io.*;
import java.util.ArrayList;

/**
 * 自动将src/main/resources/类名中in目录下的文件处理后输出到out目录
 * <br>需重写protected void modify(BufferedReader r, BufferedWriter w) throws Exception {
 * <br>可以在主方法新建自身后运行execute()方法
 * <br>可以先运行一次创建好相应目录
 */
public abstract class BaseModifyFile {

    /** 输出路径，可重写 */
    protected String getInPath() {
        return "src/main/resources/" + this.getClass().getName().replace('.', '/') + "/in";
    }

    /** 输出路径，可重写 */
    protected String getOutPath() {
        return "src/main/resources/" + this.getClass().getName().replace('.', '/') + "/out";
    }

    /** 业务逻辑，需重写 */
    protected abstract void modify(BufferedReader r, BufferedWriter w) throws Exception;

    /** 执行方法 */
    protected void execute() {
        // 创建路径
        File inPath = new File(getInPath());
        inPath.mkdirs();
        File outPath = new File(getOutPath());
        outPath.mkdirs();

        ArrayList<File> fileArrayList = new ArrayList<>();
        listDeep(fileArrayList, null, false, true, inPath);
        try {
            for (File file : fileArrayList) {
                BufferedReader r = new BufferedReader(new FileReader(file));
                BufferedWriter w = new BufferedWriter(new FileWriter(new File(getOutPath(), file.getName())));
                // 业务逻辑
                modify(r, w);
                r.close();
                w.close();
                System.out.println(file.getName() + "完成");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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