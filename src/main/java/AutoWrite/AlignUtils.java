package AutoWrite;

public class AlignUtils {

    /** Chinese, Japanese, Korean and Vietnamese char len = 2 */
    public static int displayLen(String s) {
        int len = s.length();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isIdeographic(s.charAt(i))) {
                len++;
            }
        }
        return len;
    }
}
