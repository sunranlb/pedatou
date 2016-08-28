package com.sr.pedatou.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.TextView;

import com.sr.pedatou.R;
import com.sr.pedatou.util.Note;

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
        if (tv_header != null) {
            tv_header.setText(header.toString());
        }

        if (mIsSetBgColor) {
            holder.getRootView().setBackgroundColor(Color.parseColor("#ff9900"));
        }
    }

    @Override
    public void setViewHolder(int groupId, int childId, int position, Note itemData,
                              HeaderRecycleViewHolder holder, Typeface t) {
        TextView tv_time = holder.getView(R.id.list_item_time);
        TextView tv_content = holder.getView(R.id.list_item_content);
        if (tv_content != null) {
            tv_content.setTypeface(t);
            tv_content.setText(itemData.getContent());
        }
        if (tv_time != null) {
            tv_time.setTypeface(t);
            tv_time.setText(itemData.getTime());
        }
//        System.out.println(holder.getItemViewType());

        //调用的本类里的getItemViewType
//        if (holder.getItemViewType() == 1) {
//            BubbleBoxLayout layout = (BubbleBoxLayout) holder.getRootView();
//            layout.setIsDrawableTest(true);
//            layout.setButtomText("小洪是SB,哇咔哫");
//        }
//        if (mIsSetBgColor) {
//            holder.getRootView().setBackgroundColor(Color.parseColor("#99cc99"));
//        }
    }
}