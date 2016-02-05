package org.mariotaku.mediaviewer.library;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * Created by mariotaku on 16/1/2.
 */
public abstract class MediaViewerFragment extends Fragment {

    private ProgressWheel mProgressBar;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = (ProgressWheel) view.findViewById(R.id.load_progress);
    }

    public void showProgress(boolean indeterminate, float progress) {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        if (mProgressBar.getVisibility() != View.VISIBLE) {
            activity.supportInvalidateOptionsMenu();
        }
        mProgressBar.setVisibility(View.VISIBLE);
        if (indeterminate) {
            if (mProgressBar.isSpinning()) {
                mProgressBar.spin();
            }
        } else {
            mProgressBar.setProgress(progress);
        }
    }

    public void hideProgress() {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        activity.supportInvalidateOptionsMenu();
        mProgressBar.setVisibility(View.GONE);
    }


    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup inflate = (ViewGroup) inflater.inflate(R.layout.fragment_media_viewer, container, false);
        inflate.addView(onCreateMediaView(inflater, (ViewGroup) inflate.findViewById(R.id.media_container),
                savedInstanceState));
        return inflate;
    }

    protected abstract View onCreateMediaView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);


    public void setMediaViewVisible(boolean visible) {
        final View view = getView();
        if (view == null) return;
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        activity.supportInvalidateOptionsMenu();
        final View mediaContainer = view.findViewById(R.id.media_container);
        mediaContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected abstract void recycleMedia();

}
