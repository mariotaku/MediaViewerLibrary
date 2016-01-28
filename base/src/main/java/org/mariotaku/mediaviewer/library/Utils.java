package org.mariotaku.mediaviewer.library;

import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by mariotaku on 16/1/20.
 */
public class Utils {
    public static void closeSilently(@Nullable Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException e) {
            // Ignore
        }
    }
}
