package org.ititandev.instagram.materialcamera;

import android.app.Fragment;
import android.support.annotation.NonNull;

import org.ititandev.instagram.materialcamera.internal.BaseCaptureActivity;
import org.ititandev.instagram.materialcamera.internal.CameraFragment;

public class CaptureActivity extends BaseCaptureActivity {

  @Override
  @NonNull
  public Fragment getFragment() {
    return CameraFragment.newInstance();
  }
}
