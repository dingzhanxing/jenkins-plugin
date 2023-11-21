package com.rocket.devops.rdoi.utils;

public class SeparatorHandler {
    public static String getSeparatorStart(){
        String s = Constants.LOG_SEPARATOR_SYMBOL.repeat(Integer.parseInt(Constants.LOG_SEPARATOR_SYMBOL_REPEAT_TIMES_START));
        return s;
    }
    public static String getSeparatorEnd(String content){
        int separatorEndLength = Integer.parseInt(Constants.LOG_SEPARATOR_SYMBOL_REPEAT_TIMES_END) - content.length();
        if (separatorEndLength <= 0)
            separatorEndLength = 0;
        String s = Constants.LOG_SEPARATOR_SYMBOL.repeat(separatorEndLength);
        return s;
    }

    public static String appendURLSeparator(String url){
        if (url != null){
            int pos = url.lastIndexOf("/");
            if (pos != -1 && pos != (url.length() - 1)) {
                url += "/";
            }
        }
        return url;
    }
}
