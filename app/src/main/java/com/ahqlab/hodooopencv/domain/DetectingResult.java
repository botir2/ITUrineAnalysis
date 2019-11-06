package com.ahqlab.hodooopencv.domain;

import org.opencv.core.Mat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DetectingResult {

    public static int FRATURE_DETECT_ERROR = 1;
    public static int FIRST_RECT_FIND_ERROR = 2;
    public static int LITMUS_FIND_ERROR = 3;

    @Builder.Default
    private int errorCode = 0;
    private String errorMsg;
    private Mat resultMat;
}
