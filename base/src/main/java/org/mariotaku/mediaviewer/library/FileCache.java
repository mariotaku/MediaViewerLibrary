package org.mariotaku.mediaviewer.library;

import android.net.Uri;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mariotaku on 16/1/20.
 */
@WorkerThread
public interface FileCache {
    @WorkerThread
    File get(String key) throws IOException;

    @WorkerThread
    void remove(String key) throws IOException;

    @WorkerThread
    void save(String key, InputStream stream, CopyListener listener) throws IOException;

    Uri toUri(String key);

    String fromUri(Uri uri);

    interface CopyListener {
        @WorkerThread
        boolean onCopied(int current);
    }
}
