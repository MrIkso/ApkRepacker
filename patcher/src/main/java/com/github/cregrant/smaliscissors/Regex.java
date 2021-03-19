package com.github.cregrant.smaliscissors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Regex {
    enum MatchType {
        Full,
        Split,
        SplitPath,
    }
    static ArrayList<String> matchMultiLines(Pattern readyPattern, CharSequence content, MatchType mode) {
        Matcher matcher = readyPattern.matcher(content);
        ArrayList<String> matchedArr = new ArrayList<>();
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); ++i) {
                String textMatched = matcher.group(i);
                switch (mode) {
                    case Full:
                        matchedArr.add(textMatched);
                        break;
                    case Split:
                        matchedArr.addAll(Arrays.asList(textMatched.split("\\R")));
                        break;
                    case SplitPath:
                        for (String str : textMatched.split("\\R")) {
                            matchedArr.add(str.replace("*/*", "*").trim());
                        }
                        break;
                }
            }
        }
        return matchedArr;
    }

    static String matchSingleLine(Pattern readyPattern, CharSequence content) {
        Matcher matcher = readyPattern.matcher(content);
        if (matcher.find()) {
            if (matcher.groupCount()==0)
                return matcher.group(0);
            return matcher.group(1);
        }
        return null;
    }

    static String getEndOfPath(String path) {
        int last = path.lastIndexOf('/')+1;
        return path.substring(last);
    }

    static String globToRegex(String line) {
        line = line.trim();
        int strLen = line.length();
        StringBuilder sb = new StringBuilder(strLen);
        boolean escaping = false;
        int inBraces = 0;
        char prevChar = 0;
        for (char currentChar : line.toCharArray()) {
            switch (currentChar) {
                case '*':
                    if (escaping)
                        sb.append("\\*");
                    else
                        if (currentChar != prevChar)
                            sb.append(".*");
                    escaping = false;
                    break;
                case '?':
                    if (escaping)
                        sb.append("\\?");
                    else
                        sb.append('.');
                    escaping = false;
                    break;
                case '.':
                case '(':
                case ')':
                case '+':
                case '|':
                case '^':
                case '$':
                case '@':
                case '%':
                    sb.append('\\');
                    sb.append(currentChar);
                    escaping = false;
                    break;
                case '\\':
                    if (escaping) {
                        sb.append("\\");
                        escaping = false;
                    }
                    else
                        escaping = true;
                    break;
                case '{':
                    if (escaping)
                        sb.append("\\{");
                    else {
                        sb.append('(');
                        inBraces++;
                    }
                    escaping = false;
                    break;
                case '}':
                    if (inBraces > 0 && !escaping) {
                        sb.append(')');
                        inBraces--;
                    }
                    else if (escaping)
                        sb.append("\\}");
                    else
                        sb.append("}");
                    escaping = false;
                    break;
                case ',':
                    if (inBraces > 0 && !escaping)
                        sb.append('|');
                    else if (escaping)
                        sb.append("\\,");
                    else
                        sb.append(",");
                    break;
                default:
                    escaping = false;
                    sb.append(currentChar);
            }
            prevChar = currentChar;
        }
        return sb.toString();
    }
}