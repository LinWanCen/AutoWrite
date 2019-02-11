package AutoWrite;

import java.io.File;
import java.io.FileFilter;

/**
 * 【文件拓展名】过滤器，忽略大小写
 *
 * @author linWanCheng
 */
public class NameEnd implements FileFilter {

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
