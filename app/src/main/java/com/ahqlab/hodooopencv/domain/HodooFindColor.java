package com.ahqlab.hodooopencv.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HodooFindColor {
    private int index;
    private int red;
    private int green;
    private int blue;
    private float[] hsv;

}
