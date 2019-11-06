package com.ahqlab.hodooopencv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ahqlab.hodooopencv.R;
import com.ahqlab.hodooopencv.domain.ComburResult;
import com.ahqlab.hodooopencv.util.HodooUtil;
import com.ahqlab.hodooopencv.view.ComburImageView;

import java.util.List;

import static com.ahqlab.hodooopencv.constant.HodooConstant.DEBUG;

public class ComburListAdapter extends BaseAdapter {
    private final String TAG = ComburListAdapter.class.getSimpleName();
    private Context mContext;
    private List<ComburResult> mResults;
    private LayoutInflater mInflater;
    public ComburListAdapter (Context context, List<ComburResult> results) {
        mContext = context;
        mResults = results;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return mResults.size();
    }

    @Override
    public Object getItem(int position) {
        return mResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if ( convertView == null ) {
            convertView = mInflater.inflate(R.layout.item_combur_result, parent, false);
            holder = new ViewHolder();
            holder.imgviews = new ImageView[11];
            holder.comburTitle = convertView.findViewById(R.id.combur_title);
            holder.resultStr = convertView.findViewById(R.id.result_str);
            holder.comburColorWrap = convertView.findViewById(R.id.combur_color_wrap);
            if ( DEBUG ) Log.e(TAG, String.format("imageViews : %d", mResults.get(position).getImageViews().length));

            for (int i = 0; i < 11; i++) {
                holder.imgviews[i] = convertView.findViewById( mContext.getResources().getIdentifier("color_" + String.format("%02d", i + 1), "id", mContext.getPackageName()) );
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        for ( int i = 0; i < mResults.get(position).getImgs().length; i++ ) {
            holder.imgviews[i].setImageResource(mResults.get(position).getImgs()[i]);
        }

        holder.comburTitle.setText(mResults.get(position).getComburTitle());
        holder.resultStr.setText(mResults.get(position).getResultMsg());
        return convertView;
    }
    private class ViewHolder {
        private TextView comburTitle;
        private TextView resultStr;
        private LinearLayout comburColorWrap;
        private TextView test;
        private ImageView[] imgviews;
    }

}
