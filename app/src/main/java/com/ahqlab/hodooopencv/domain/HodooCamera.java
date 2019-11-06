package com.ahqlab.hodooopencv.domain;

import android.hardware.Camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HodooCamera {
    @Builder.Default
    private int cameraDirection = Camera.CameraInfo.CAMERA_FACING_BACK;
}
