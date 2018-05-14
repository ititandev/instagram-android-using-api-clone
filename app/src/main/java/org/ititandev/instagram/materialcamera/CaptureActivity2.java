package org.ititandev.instagram.materialcamera;

import android.app.Fragment;
import android.support.annotation.NonNull;

import org.ititandev.instagram.materialcamera.internal.BaseCaptureActivity;
import org.ititandev.instagram.materialcamera.internal.Camera2Fragment;


public class CaptureActivity2 extends BaseCaptureActivity {

  @Override
  @NonNull
  public Fragment getFragment() {
    return Camera2Fragment.newInstance();
  }
}
