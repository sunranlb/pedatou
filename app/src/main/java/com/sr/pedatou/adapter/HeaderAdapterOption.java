package com.sr.pedatou.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.widget.ImageView;
import android.widget.TextView;

import com.sr.pedatou.R;
import com.sr.pedatou.util.Note;
import com.sr.pedatou.util.Tools;

/**
 * Created by taro on 16/6/22.
 */
public class HeaderAdapterOption implements HeaderRecycleAdapter.IHeaderAdapterOption<Note,
        String> {
    private boolean mIsMultiType = false;
    private boolean mIsSetBgColor = false;

    public HeaderAdapterOption(boolean isMultiType, boolean isSetBgColor) {
        mIsMultiType = isMultiType;
        mIsSetBgColor = isSetBgColor;
    }

    // 返回Header的类型。此demo中只有一种，因为mIsMultiType设置为false了
    @Override
    public int getHeaderViewType(int groupId, int position) {
        if (mIsMultiType) {
            if (groupId > 6) {
                return -3;
            } else if (groupId > 3) {
                return -1;
            } else {
                return -2;
            }
        } else {
            return -1;
        }
    }

    // childId为-1~4的值（Y？）childId==-1的时候isHeaderItem==true
    @Override
    public int getItemViewType(int position, int groupId, int childId, boolean isHeaderItem,
                               boolean isShowHeader) {
//        System.out.println("po:"+position+"gid:"+groupId+"chid"+childId+"isHI:"+isHeaderItem
// +"iSH:"+isShowHeader);
        if (isHeaderItem) {
            int ret = getHeaderViewType(groupId, position);
//            System.out.println("ret:"+ret);
            return ret;
        } else {
            if (mIsMultiType) {
                if (childId > 3) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        }
    }

    @Override
    public int getLayoutId(int viewType) {
//        System.out.println(""+viewType);
        switch (viewType) {
            case -1:
                return R.layout.item_header;
            default:
                return R.layout.list_item;
        }
    }

    @Override
    public void setHeaderHolder(int groupId, String header, HeaderRecycleViewHolder holder) {
        TextView tv_header = holder.getView(R.id.tv_header);
        ImageView iv_header = holder.getView(R.id.line_header);
        if (tv_header != null) {
            tv_header.setText(header.toString());
        }
        switch (groupId) {
            case 0:
                iv_header.setImageResource(R.drawable.header_line_history);
                break;
            case 1:
                iv_header.setImageResource(R.drawable.header_line_today);
                break;
            case 2:
                iv_header.setImageResource(R.drawable.header_line_tomorror);
                break;
            case 3:
                iv_header.setImageResource(R.drawable.header_line_within_one_week);
                break;
            case 4:
                iv_header.setImageResource(R.drawable.header_line_within_two_weeks);
                break;
            case 5:
                iv_header.setImageResource(R.drawable.header_line_farther_future);
                break;
            default:
        }
//        if (mIsSetBgColor) {
//            holder.getRootView().setBackgroundColor(Color.parseColor("#ff9900"));
//        }
    }

    @Override
    public void setViewHolder(int groupId, int childId, int position, Note itemData,
                              HeaderRecycleViewHolder holder, Typeface t) {
//        System.out.println("gid:"+groupId+",cid:"+childId+"pos:"+position);
        TextView tv_time = holder.getView(R.id.list_item_time);
        TextView tv_content = holder.getView(R.id.list_item_content);
        CardView rootView = (CardView) holder.getRootView();
        if (tv_content != null) {
            tv_content.setTypeface(t);
            tv_content.setText(itemData.getContent());
            if (groupId == 0) tv_content.setTextColor(Color.GRAY);
            else tv_content.setTextColor(Color.BLACK);
        }

        if (tv_time != null) {
            tv_time.setTypeface(t);
            tv_time.setText(Tools.transDB2RV(itemData.getTime()));
            if (groupId == 0) tv_time.setTextColor(Color.GRAY);
            else tv_time.setTextColor(Color.BLACK);
        }
        switch (groupId) {

            case 1:
                rootView.setCardBackgroundColor(Color.parseColor("#f44336"));
                break;
            case 2:
                rootView.setCardBackgroundColor(Color.parseColor("#ef9a9a"));
                break;
            case 3:
                rootView.setCardBackgroundColor(Color.parseColor("#ffcdd2"));
                break;
            case 4:
                rootView.setCardBackgroundColor(Color.parseColor("#ffebee"));
                break;
            default:
                rootView.setCardBackgroundColor(Color.parseColor("#ffffff"));
        }
//        System.out.println(holder.getItemViewType());

        //调用的本类里的getItemViewType
//        if (holder.getItemViewType() == 1) {
//            BubbleBoxLayout layout = (BubbleBoxLayout) holder.getRootView();
//            layout.setIsDrawableTest(true);
//            layout.setButtomText("小洪是SB,哇咔哫");
//        }
    }
}