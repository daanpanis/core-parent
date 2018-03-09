package com.daanpanis.core.api.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class RegexUtil {

    /**
     * @param string
     * @param regex
     * @return
     */
    public static List<String> getAllMatches(String string, String regex) {
        return getAllMatches(string, Pattern.compile(regex));
    }

    /**
     * @param string
     * @param pattern
     * @return
     */
    public static List<String> getAllMatches(String string, Pattern pattern) {
        List<String> list = new ArrayList<>();
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    /**
     * <p>This method will try to find a pattern in a given string, an will return the first
     * occurrence of your pattern</p>
     *
     * @param string        The string to search in
     * @param patternString The pattern to search for
     * @return Returns the first occurrence of a pattern found in a string
     * @see Pattern
     * @see Matcher
     */
    public static String getFirstOccurrence(String string, String patternString) {
        Matcher matcher = Pattern.compile(patternString).matcher(string);
        return matcher.find() ? matcher.group() : "";
    }

    /**
     * A private constructor to make sure no one initializes the utility class as that would be pointless.
     */
    private RegexUtil() {
    }

}
