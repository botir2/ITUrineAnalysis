package com.ahqlab.hodooopencv.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ahqlab.hodooopencv.R;
import com.ahqlab.hodooopencv.domain.ComburResult;
import com.ahqlab.hodooopencv.util.HodooUtil;

import org.opencv.core.Rect;

import java.util.List;

public class CustomColorScrollView extends ScrollView {
    public interface ColorListCallback {
        void setOnItemClickListener( int position );
    }
    public CustomColorScrollView(Context context) {
        this(context, null);
    }

    public CustomColorScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.scrollViewStyle);
    }

    public CustomColorScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setItem (List<ComburResult> colors, final ColorListCallback callback) {
        LinearLayout wrap = new LinearLayout(getContext());
        wrap.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(HodooUtil.dpToPx(50), ViewGroup.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < colors.size(); i++) {
            LinearLayout innerWrap = new LinearLayout(getContext());
            final int finalI = i;
            innerWrap.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.setOnItemClickListener(finalI);
                }
            });
            innerWrap.setBackgroundResource(R.drawable.top_border);
            innerWrap.setPadding(HodooUtil.dpToPx(20), HodooUtil.dpToPx(20), 0, HodooUtil.dpToPx(20));
            innerWrap.setGravity(Gravity.CENTER_VERTICAL);
            innerWrap.setOrientation(LinearLayout.HORIZONTAL);

            TextView title = new TextView(getContext());
            title.setLayoutParams(textParams);
            title.setText(colors.get(i).getComburTitle());
            TextView result = new TextView(getContext());
            result.setLayoutParams(textParams);
            result.setText(colors.get(i).getResultMsg());
            LinearLayout colorImgWrap = new LinearLayout(getContext());
            colorImgWrap.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(HodooUtil.dpToPx(20), HodooUtil.dpToPx(20));
            params.setMargins(HodooUtil.dpToPx(5), 0, 0, 0);
            for ( int j = 0; j < colors.get(i).getImgs().length; j++ ) {
                ImageView colorImg = new ImageView(getContext());
                colorImg.setPadding(HodooUtil.dpToPx(1), HodooUtil.dpToPx(1), HodooUtil.dpToPx(1), HodooUtil.dpToPx(1));
                colorImg.setLayoutParams(params);
                colorImg.setImageResource(colors.get(i).getImgs()[j]);
                if ( j == colors.get(i).getResultPosition() )
                    colorImg.setBackgroundResource(R.drawable.board_rect);
                colorImgWrap.addView(colorImg);
            }

            innerWrap.addView(title);
            innerWrap.addView(result);
            innerWrap.addView(colorImgWrap);


            wrap.addView(innerWrap);
        }
        addView(wrap);
    }
}
