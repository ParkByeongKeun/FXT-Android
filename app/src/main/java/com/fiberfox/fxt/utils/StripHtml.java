package com.fiberfox.fxt.utils;

import android.text.Html;

public class StripHtml {
    public static String split(String html) {
        return Html.fromHtml(html).toString();
    }
}
