package com.sr.pedatou.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.sr.pedatou.R;
import com.sr.pedatou.util.Note;

import java.util.ArrayList;
import java.util.List;

import static com.sr.pedatou.util.Tools.transDB2RV;

/**
 * Created by SR on 2016/7/24.
 */
public class RVAdapter extends RecyclerView.Adapter {
    private OnRecyclerViewListener onRecyclerViewListener;
    private List<Note> list;
    private Typeface typeface;
    private int lastAnimatedPosition = -1;
    private boolean animationsLocked = false;

    public RVAdapter(List<Note> list, Typeface t) {
        this.list = list;
        this.typeface = t;
    }

    public RVAdapter(Typeface typeface) {
        this.list = new ArrayList<Note>();
        this.typeface = typeface;
    }

    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener) {
        this.onRecyclerViewListener = onRecyclerViewListener;
    }

    public Note getItem(int p) {
        return list.get(p);
    }

    public List<Note> getDataList() {
        return list;
    }

    public void changeOneNoteContent(int position, String content) {
        if (position != RecyclerView.NO_POSITION) {
            list.get(position).setContent(content);
            notifyItemChanged(position);
        }
    }

    public void addList(List<Note> l) {
        list.clear();
//        for (int i = 0; i < l.size(); ++i) {
//            list.add(l.get(i));
//            notifyItemRangeInserted(i, i + 1);
//        }
        list.addAll(l);
        notifyItemRangeInserted(0, l.size());
    }

    public void add(int position, Note note) {
//        if (position != RecyclerView.NO_POSITION) {
        list.add(position, note);
        notifyItemInserted(position);
//        }
    }

    public void removeById(int id) {
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i).getId() == id) {
                list.remove(list.get(i));
                notifyItemRemoved(i);
                return;
            }
        }
    }

    public boolean remove(int position) {
//        if (position != RecyclerView.NO_POSITION) {
        list.remove(list.get(position));
        notifyItemRemoved(position);
//        }
        return true;
    }

    // 该函数在每一个VH新建的时候调用，创建新的VH。
    // 当VH消失在屏幕后再次需要展现的时候不会被调用，而是使用现有的
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
        return new NoteViewHolder(view);
    }

    // 该函数在每个VH需要展现在屏幕中的时候被调用
    // RV有一些VH缓存，在VH消失在屏幕的瞬间并不会被回收而是被缓存
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
//        System.out.println("onBind");
//        runEnterAnimation(viewHolder.itemView, i);
        NoteViewHolder holder = (NoteViewHolder) viewHolder;
        Note n = list.get(i);
        holder.time.setTypeface(typeface);
        holder.time.setText(transDB2RV(n.getTime()));
        holder.content.setTypeface(typeface);
        holder.content.setText(n.getContent());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void runEnterAnimation(View view, int position) {
        if (animationsLocked) return;
        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setAlpha(0.f);
            view.animate()
                    .translationY(0).alpha(1.f)
                    .setStartDelay(50 * (position))
                    .setInterpolator(new DecelerateInterpolator(2.f))
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animationsLocked = true;
                        }
                    })
                    .start();
        }
    }

    public static interface OnRecyclerViewListener {
        void onItemClick(int position);

        boolean onItemLongClick(int position);
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public View rootView;
        public TextView time;
        public TextView content;

        public NoteViewHolder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.list_item_time);
            content = (TextView) itemView.findViewById(R.id.list_item_content);
            rootView = itemView.findViewById(R.id.list_item);
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (null != onRecyclerViewListener) {
                onRecyclerViewListener.onItemClick(this.getPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (null != onRecyclerViewListener) {
                return onRecyclerViewListener.onItemLongClick(this.getPosition());
            }
            return false;
        }
    }
}
