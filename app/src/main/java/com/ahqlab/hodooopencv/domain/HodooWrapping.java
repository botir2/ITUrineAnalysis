package com.ahqlab.hodooopencv.domain;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HodooWrapping {
    private String fileName;
    private Mat target;
    private List<Point> points;
    private Point tl;
    private Point tr;
    private Point bl;
    private Point br;
}
