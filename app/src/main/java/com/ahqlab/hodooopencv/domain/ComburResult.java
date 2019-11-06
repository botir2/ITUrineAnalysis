package com.ahqlab.hodooopencv.domain;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.ahqlab.hodooopencv.view.ComburImageView;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import static com.ahqlab.hodooopencv.constant.HodooConstant.DEBUG;

@Data
@Builder
@AllArgsConstructor
public class ComburResult {
    public static final int COMBUR_ROTATION_0 = 0;
    public static final int COMBUR_ROTATION_90 = 1;
    public static final int COMBUR_ROTATION_180 = 2;

    private String comburTitle;
    private int position;
    private int resultPosition;
    private String resultMsg;
    private String detectColor;
    private int[] imgs;
    public static final int[] maxNum = {
      7, 5, 4, 2, 4, 5, 4, 5, 4, 5, 5
    };
    private ImageView[] imageViews;
    private final String imgPreStr;
    public void imgSetting (Context context) {
        imgs = new int[maxNum[position]];
        imageViews = new ImageView[maxNum[position]];
        for (int i = 0; i < maxNum[position]; i++) {
            imgs[i] = context.getResources().getIdentifier(imgPreStr + "_" + String.format("%02d", i + 1), "drawable", context.getPackageName());
            ImageView img = new ImageView(context);
            img.setImageResource(imgs[i]);
            imageViews[i] = img;
        }

    }

}
