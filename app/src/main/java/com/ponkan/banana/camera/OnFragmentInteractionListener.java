package com.ponkan.banana.camera;

import android.net.Uri;

public interface OnFragmentInteractionListener {
    void go2ImagePreview(String imagePath);

    void onFragmentInteraction(Uri uri);
}
