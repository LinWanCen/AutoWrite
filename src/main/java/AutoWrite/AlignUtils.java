package AutoWrite;

import java.util.regex.Pattern;

public class AlignUtils {

    public static final Pattern LEN_1_PATTERN = Pattern.compile("[\\u0000-\\u00FF]");

    public static int displayLen(String s) {
        return s.length() + LEN_1_PATTERN.matcher(s).replaceAll("").length();
    }
}
