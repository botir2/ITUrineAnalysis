package com.ahqlab.hodooopencv.domain;

import android.content.Context;

import com.ahqlab.hodooopencv.R;

import lombok.Data;

@Data
public class HsvValue {
    private String sg;
    private String ph;
    private String leu;
    private String nit;
    private String pro;
    private String glu;
    private String ket;
    private String ubg;
    private String bil;
    private String ery;
    private String hb;
    public String[] toArray () {
        String[] array = new String[11];
        array[0] = sg;
        array[1] = ph;
        array[2] = leu;
        array[3] = nit;
        array[4] = pro;
        array[5] = glu;
        array[6] = ket;
        array[7] = ubg;
        array[8] = bil;
        array[9] = ery;
        array[10] = hb;
        return array;
    }


}
