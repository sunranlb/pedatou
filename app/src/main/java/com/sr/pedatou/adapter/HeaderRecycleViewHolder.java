package com.sr.pedatou.adapter;

import android.graphics.Point;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sr.pedatou.R;

/**
 * Created by taro on 16/4/19.
 */
public class HeaderRecycleViewHolder extends RecyclerView.ViewHolder implements View
        .OnClickListener, View.OnLongClickListener {
    /**
     * 根布局的ID,使用0
     */
    public static final int ROOT_VIEW_ID = 0;

    private int mGroupId = -1;
    private int mChildId = -1;
    private View mRootView;
    private TextView mTime;
    private TextView mContent;
    private OnItemClickListener mRootViewClickListener = null;
    private HeaderRecycleAdapter mParentAdapter = null;
    //View缓存
    private ArrayMap<Integer, View> mViewHolder = null;

    /**
     * 带adapter的holder,推荐使用此方法(很常会用到adapter)
     *
     * @param adapter
     * @param itemView holder的rootView
     */
    public HeaderRecycleViewHolder(HeaderRecycleAdapter adapter, View itemView) {
        super(itemView);
        mRootView = itemView;
        mTime = (TextView) itemView.findViewById(R.id.list_item_time);
        mContent = (TextView) itemView.findViewById(R.id.list_item_content);
        mParentAdapter = adapter;
        mViewHolder = new ArrayMap<Integer, View>();
    }

    /**
     * 设置holder的GroupID及当前项在在该组的位置ID
     *
     * @param groupId 分组ID,从0开始
     * @param childId 组内元素ID,从0开始.当childID为负数时,当前项为该组header
     */
    public void setGroupIdAndChildId(int groupId, int childId) {
        mGroupId = groupId;
        mChildId = childId;
    }

    /**
     * 获取分组ID
     *
     * @return
     */
    public int getGroupId() {
        return mGroupId;
    }

    /**
     * 获取当前项在组内的位置ID,当此值为负数时,当前项为该组的Header
     *
     * @return
     */
    public int getChildId() {
        return mChildId;
    }

    /**
     * 获取当前项是否为该组的Header,实际判断方式为当childID为负数时,当前项即为header
     *
     * @return
     */
    public boolean isHeaderItem() {
        return mChildId < 0;
    }

    /**
     * 从当前item中查找指定ID的View,view仅会查找一次并缓存其引用
     *
     * @param viewId 查找viewID
     * @param <T>
     * @return
     */
    public <T extends View> T getView(int viewId) {
        View view = mViewHolder.get(viewId);
        if (view == null) {
            view = mRootView.findViewById(viewId);
            mViewHolder.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 清除View的缓存
     */
    public void clearViewCache() {
        mViewHolder.clear();
    }

    /**
     * 为textView设置文本
     *
     * @param viewId
     * @param text
     * @return
     */
    public HeaderRecycleViewHolder setTextInTextView(int viewId, String text) {
        TextView tv = this.getView(viewId);
        tv.setText(text);
        return this;
    }

    /**
     * 为imageView设置图片资源
     *
     * @param viewId
     * @param imgResID
     * @return
     */
    public HeaderRecycleViewHolder setImageInImageView(int viewId, int imgResID) {
        ImageView iv = this.getView(viewId);
        iv.setImageResource(imgResID);
        return this;
    }

    /**
     * 为View设置背景色
     *
     * @param viewId
     * @param color  颜色值,不是颜色的资源ID
     * @return
     */
    public HeaderRecycleViewHolder setBackgroundColorInView(int viewId, int color) {
        View v = this.getView(viewId);
        v.setBackgroundColor(color);
        return this;
    }

    /**
     * 为View设置背景资源
     *
     * @param viewId
     * @param resId
     * @return
     */
    public HeaderRecycleViewHolder setBackgroundResourceInView(int viewId, int resId) {
        View v = this.getView(viewId);
        v.setBackgroundResource(resId);
        return this;
    }

    /**
     * 注册rootView的单击响应事件,注册了rootView的响应事件时,将不会响应其子控件的单击事件
     *
     * @param listener
     */
    public void registerRootViewItemClickListener(OnItemClickListener listener) {
        mRootViewClickListener = listener;
        mRootView.setOnClickListener(this);
        mRootView.setOnLongClickListener(this);
    }


    /**
     * 获取根布局
     *
     * @return
     */
    public View getRootView() {
        return mRootView;
    }

    public TextView getTime() {
        return mTime;
    }

    public TextView getContent() {
        return mContent;
    }

    /**
     * 获取父adapter
     *
     * @return
     */
    public HeaderRecycleAdapter getAdatper() {
        return mParentAdapter;
    }

    @Override
    public void onClick(View v) {
        int position = getAdapterPosition();
        Point p = mParentAdapter.getGroupIdAndChildIdFromPosition(mParentAdapter.getEachGroupCountList(), position, true);
        setGroupIdAndChildId(p.x, p.y);
        mRootViewClickListener.onItemClick(mGroupId, mChildId, position,
                ROOT_VIEW_ID, this.isHeaderItem(), mRootView, this);
    }

    @Override
    public boolean onLongClick(View v) {
        int position = getAdapterPosition();
        Point p = mParentAdapter.getGroupIdAndChildIdFromPosition(mParentAdapter.getEachGroupCountList(), position, true);
        setGroupIdAndChildId(p.x, p.y);
        mRootViewClickListener.onItemLongClick(mGroupId, mChildId, position,
                ROOT_VIEW_ID, this.isHeaderItem(), mRootView, this);
        return true;
    }

    /**
     * itemClick事件
     */
    public interface OnItemClickListener {
        /**
         * 带header的item单击事件
         *
         * @param groupId  当前item所在分组,分组ID从0开始
         * @param childId  当前item在所有分组中的ID,从0开始,当此值为-1时,当前为该分组的头部
         * @param position 当前item所有分组的位置(header也会占用一个位置,请注意)
         * @param viewId   当前响应的单击事件view的ID,若为rootView,则该值为{@link #ROOT_VIEW_ID}
         * @param isHeader 当前item是否为header
         * @param rootView 当前item的rootView
         * @param holder
         */
        void onItemClick(int groupId, int childId, int position, int viewId, boolean isHeader,
                         View rootView, HeaderRecycleViewHolder holder);

        void onItemLongClick(int groupId, int childId, int position, int viewId, boolean
                isHeader, View rootView, HeaderRecycleViewHolder holder);
    }

}
