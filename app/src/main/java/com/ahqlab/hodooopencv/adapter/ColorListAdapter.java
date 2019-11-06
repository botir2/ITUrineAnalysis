package com.ahqlab.hodooopencv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ahqlab.hodooopencv.R;
import com.ahqlab.hodooopencv.domain.HodooFindColor;

import org.opencv.core.Rect;

import java.util.List;

public class ColorListAdapter extends BaseAdapter {
    private Context mContext;
    private List<HodooFindColor> mColors;
    private LayoutInflater mInflater;
    private List<Rect> mRects;
    private ColorListCallback mCallback;
    public interface ColorListCallback {
        void setOnItemClickListener( int position );
    }

    public ColorListAdapter (Context context, List<HodooFindColor> colors, List<Rect> rects, ColorListCallback callback) {
        mContext = context;
        mColors = colors;
        mRects = rects;
        mCallback = callback;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return mColors.size();
    }

    @Override
    public Object getItem(int position) {
        return mColors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if ( convertView == null ) {
            convertView = mInflater.inflate(R.layout.item_color, parent, false);
            holder = new ViewHolder();
            holder.colorBox = convertView.findViewById(R.id.color_box);
            holder.index = convertView.findViewById(R.id.index);
            holder.colorCode = convertView.findViewById(R.id.color_code);
            holder.hueColor = convertView.findViewById(R.id.hue_color);
            holder.saturationColor = convertView.findViewById(R.id.saturation_color);
            holder.valueColor = convertView.findViewById(R.id.value_color);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( mCallback != null )
                    mCallback.setOnItemClickListener(position);
            }
        });
        String hex = String.format("#%02x%02x%02x", mColors.get(position).getRed(), mColors.get(position).getGreen(),mColors.get(position).getBlue());
        holder.colorBox.setBackgroundColor(Color.parseColor(hex) );
        holder.index.setText( String.valueOf(mColors.get(position).getIndex()) );
        holder.colorCode.setText(hex);
        float[] hsv = mColors.get(position).getHsv();
        holder.hueColor.setText( "H : " + String.valueOf( (int) hsv[0] ) +"˚" );
        holder.saturationColor.setText( "S : " + String.format("%.1f", (hsv[1] * 100)) +"%" );
        holder.valueColor.setText( "V : " + String.format("%.1f", (hsv[2] * 100)) +"%" );
        return convertView;
    }
    private class ViewHolder {
        private View colorBox;
        private TextView index;
        private TextView colorCode;
        private TextView hueColor;
        private TextView saturationColor;
        private TextView valueColor;
    }
}
