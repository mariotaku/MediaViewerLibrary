package org.mariotaku.mediaviewer.library.subsampleimageview;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.mariotaku.mediaviewer.library.AbsMediaViewerActivity;
import org.mariotaku.mediaviewer.library.CacheDownloadLoader;
import org.mariotaku.mediaviewer.library.MediaViewerFragment;


/**
 * Created by mariotaku on 16/1/20.
 */
public class SubsampleImageViewerFragment extends MediaViewerFragment implements CacheDownloadLoader.Listener,
        LoaderManager.LoaderCallbacks<CacheDownloadLoader.Result>,
        View.OnClickListener {

    public static final String EXTRA_MEDIA_URI = "media_url";

    private SubsamplingScaleImageView mImageView;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageView = (SubsamplingScaleImageView) view.findViewById(R.id.image_view);
    }

    @Override
    protected View onCreateMediaView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_media_viewer_subsample_image_view, container, false);
    }

    @Override
    public void onClick(View v) {
        final AbsMediaViewerActivity activity = (AbsMediaViewerActivity) getActivity();
        if (activity == null) return;
        activity.toggleBar();
    }


    @Override
    protected boolean isAbleToLoad() {
        return true;
    }

    @Override
    protected Uri getDownloadUri() {
        return getArguments().getParcelable(EXTRA_MEDIA_URI);
    }

    @Override
    protected Object getDownloadExtra() {
        return null;
    }

    @Override
    protected void displayMedia(CacheDownloadLoader.Result data) {
        mImageView.setImage(ImageSource.uri(data.cacheUri));
    }


    @Override
    protected void recycleMedia() {
        mImageView.recycle();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        mImageView.setOnClickListener(this);
        startLoading();
    }

    public static SubsampleImageViewerFragment get(Uri mediaUri) {
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_MEDIA_URI, mediaUri);
        final SubsampleImageViewerFragment f = new SubsampleImageViewerFragment();
        f.setArguments(args);
        return f;
    }
}
