package com.sr.pedatou.others;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.sr.pedatou.adapter.RVAdapter;

import java.util.List;

/**
 * Created by SR on 2016/8/17.
 */

public class MyItemAnimator extends DefaultItemAnimator {

    // stateless interpolators re-used for every change animation
    private AccelerateInterpolator mAccelerateInterpolator = new AccelerateInterpolator();
    private DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator();

    // Maps to hold running animators. These are used when running a new change
    // animation on an item that is already being animated. mRunningAnimators is
    // used to cancel the previous animation. mAnimatorMap is used to construct
    // the new change animation based on where the previous one was at when it
    // was interrupted.
    private ArrayMap<RecyclerView.ViewHolder, AnimatorInfo> mAnimatorMap = new ArrayMap<>();

    @Override
    public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
        // This allows our custom change animation on the contents of the holder instead
        // of the default behavior of replacing the viewHolder entirely
        return true;
    }

    @NonNull
    private ItemHolderInfo getItemHolderInfo(RVAdapter.NoteViewHolder viewHolder, NoteInfo info) {
        info.time = viewHolder.time.getText().toString();
        info.content = viewHolder.content.getText().toString();
        return info;
    }

    @NonNull
    @Override
    public ItemHolderInfo recordPreLayoutInformation(RecyclerView.State state,
                                                     RecyclerView.ViewHolder viewHolder, int changeFlags, List<Object> payloads) {
        NoteInfo info = (NoteInfo) super.recordPreLayoutInformation(state, viewHolder,
                changeFlags, payloads);
        return getItemHolderInfo((RVAdapter.NoteViewHolder) viewHolder, info);
    }

    @NonNull
    @Override
    public ItemHolderInfo recordPostLayoutInformation(@NonNull RecyclerView.State state,
                                                      @NonNull RecyclerView.ViewHolder viewHolder) {
        NoteInfo info = (NoteInfo) super.recordPostLayoutInformation(state, viewHolder);
        return getItemHolderInfo((RVAdapter.NoteViewHolder) viewHolder, info);
    }

    @Override
    public ItemHolderInfo obtainHolderInfo() {
        return new NoteInfo();
    }

    /**
     * Custom change animation. Fade to black on the container background, then back
     * up to the new bg coolor. Meanwhile, the text rotates, switching along the way.
     * If a new change animation occurs on an item that is currently animating
     * a change, we stop the previous change and start the new one where the old
     * one left off.
     */
    @Override
    public boolean animateChange(@NonNull final RecyclerView.ViewHolder oldHolder,
                                 @NonNull final RecyclerView.ViewHolder newHolder,
                                 @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {

        if (oldHolder != newHolder) {
            // use default behavior if not re-using view holders
            return super.animateChange(oldHolder, newHolder, preInfo, postInfo);
        }

        final RVAdapter.NoteViewHolder viewHolder = (RVAdapter.NoteViewHolder) newHolder;

        // Get the pre/post change values; these are what we are animating between
        NoteInfo oldInfo = (NoteInfo) preInfo;
        NoteInfo newInfo = (NoteInfo) postInfo;
        final String oldTime = oldInfo.time;
        final String newTime = newInfo.time;
        final String oldContent = oldInfo.content;
        final String newContent = newInfo.content;

        // These are the objects whose values will be animated
        final TextView newTimeTextView = viewHolder.time;
        final TextView newContentTextView = viewHolder.content;

        // Check to see if there's a change animation already running on this item
        AnimatorInfo runningInfo = mAnimatorMap.get(newHolder);
        long prevAnimPlayTime = 0;
        boolean firstHalf = false;
        if (runningInfo != null) {
            // The information we need to construct the new animators is whether we
            // are in the 'first half' (fading to black and rotating the old text out)
            // and how far we are in whichever half is running
            firstHalf = runningInfo.oldTimeTextRotator != null &&
                    runningInfo.oldTimeTextRotator.isRunning();
            prevAnimPlayTime = firstHalf ?
                    runningInfo.oldTimeTextRotator.getCurrentPlayTime() :
                    runningInfo.newTimeTextRotator.getCurrentPlayTime();
            // done with previous animation - cancel it
            runningInfo.overallAnim.cancel();
        }

        // Construct the fade to/from black animation
        ObjectAnimator fadeToBlack = null, fadeFromBlack = null;
        if (runningInfo == null || firstHalf) {
            // The first part of the animation fades to black. Skip this phase
            // if we're interrupting an animation that was already in the second phase.
//            int startColor = oldColor;
//            if (runningInfo != null) {
//                startColor = (Integer) runningInfo.fadeToBlackAnim.getAnimatedValue();
//            }
//            fadeToBlack = ObjectAnimator.ofInt(newContainer, "backgroundColor",
//                    startColor, Color.BLACK);
//            fadeToBlack.setEvaluator(mColorEvaluator);
            if (runningInfo != null) {
                // Seek to appropriate time in new animator if we were already
                // running a previous animation
                fadeToBlack.setCurrentPlayTime(prevAnimPlayTime);
            }
        }

        // Second phase of animation fades from black to the new bg color
//        fadeFromBlack = ObjectAnimator.ofInt(newContainer, "backgroundColor",
//                Color.BLACK, newColor);
//        fadeFromBlack.setEvaluator(mColorEvaluator);
        if (runningInfo != null && !firstHalf) {
            // Seek to appropriate time in new animator if we were already
            // running a previous animation
            fadeFromBlack.setCurrentPlayTime(prevAnimPlayTime);
        }

        // Set up an animation to play both the first (if non-null) and second phases
        AnimatorSet bgAnim = new AnimatorSet();
        if (fadeToBlack != null) {
            bgAnim.playSequentially(fadeToBlack, fadeFromBlack);
        } else {
            bgAnim.play(fadeFromBlack);
        }
        // ----------------------------------------------------------------------
        // The other part of the animation rotates the text, switching it to the
        // new value half-way through (when it is perpendicular to the user)
        ObjectAnimator oldTimeTextRotate = null, newTimeTextRotate, oldContentTextRotate = null, newContentTextRotate;
        if (runningInfo == null || firstHalf) {
            // The first part of the animation rotates text to be perpendicular to user.
            // Skip this phase if we're interrupting an animation that was already
            // in the second phase.
            oldTimeTextRotate = ObjectAnimator.ofFloat(newTimeTextView, View.ROTATION_X, 0, 90);
            oldTimeTextRotate.setInterpolator(mAccelerateInterpolator);
            oldContentTextRotate = ObjectAnimator.ofFloat(newContentTextView, View.ROTATION_X, 0, 90);
            oldContentTextRotate.setInterpolator(mAccelerateInterpolator);
            if (runningInfo != null) {
                oldTimeTextRotate.setCurrentPlayTime(prevAnimPlayTime);
                oldContentTextRotate.setCurrentPlayTime(prevAnimPlayTime);
            }

            oldTimeTextRotate.addListener(new AnimatorListenerAdapter() {
                boolean mCanceled = false;

                @Override
                public void onAnimationStart(Animator animation) {
                    // text was changed as part of the item change notification. Change
                    // it back for the first phase of the animation
                    newTimeTextView.setText(oldTime);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCanceled = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!mCanceled) {
                        // Set it to the new text when the old rotator ends - this is when
                        // it is perpendicular to the user (thus making the switch
                        // invisible)
                        newTimeTextView.setText(newTime);
                    }
                }
            });

            oldContentTextRotate.addListener(new AnimatorListenerAdapter() {
                boolean mCanceled = false;

                @Override
                public void onAnimationStart(Animator animation) {
                    // text was changed as part of the item change notification. Change
                    // it back for the first phase of the animation
                    newContentTextView.setText(oldContent);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCanceled = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!mCanceled) {
                        // Set it to the new text when the old rotator ends - this is when
                        // it is perpendicular to the user (thus making the switch
                        // invisible)
                        newContentTextView.setText(newContent);
                    }
                }
            });
        }

        // Second half of text rotation rotates from perpendicular to 0
        newTimeTextRotate = ObjectAnimator.ofFloat(newTimeTextView, View.ROTATION_X, -90, 0);
        newTimeTextRotate.setInterpolator(mDecelerateInterpolator);
        newContentTextRotate = ObjectAnimator.ofFloat(newContentTextView, View.ROTATION_X, -90, 0);
        newContentTextRotate.setInterpolator(mDecelerateInterpolator);
        if (runningInfo != null && !firstHalf) {
            // If we're interrupting a previous second-phase animation, seek to that time
            newTimeTextRotate.setCurrentPlayTime(prevAnimPlayTime);
            newContentTextRotate.setCurrentPlayTime(prevAnimPlayTime);
        }

        // Choreograph first and second half. First half may be null if we interrupted
        // a second-phase animation
        AnimatorSet timeAnim = new AnimatorSet();
        if (oldTimeTextRotate != null) {
            timeAnim.playSequentially(oldTimeTextRotate, newTimeTextRotate);
        } else {
            timeAnim.play(newTimeTextRotate);
        }
        AnimatorSet contentAnim = new AnimatorSet();
        if (oldContentTextRotate != null) {
            contentAnim.playSequentially(oldContentTextRotate, newContentTextRotate);
        } else {
            contentAnim.play(newContentTextRotate);
        }

        // Choreograph both animations: color fading and text rotating
        AnimatorSet changeAnim = new AnimatorSet();
        changeAnim.playTogether(bgAnim, contentAnim, timeAnim);
        changeAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dispatchAnimationFinished(newHolder);
                mAnimatorMap.remove(newHolder);
            }
        });
        changeAnim.start();

        // Store info about this animation to be re-used if a succeeding change event
        // occurs while it's still running
        AnimatorInfo runningAnimInfo = new AnimatorInfo(changeAnim, fadeToBlack, fadeFromBlack,
                oldTimeTextRotate, newTimeTextRotate, oldContentTextRotate, newContentTextRotate);
        mAnimatorMap.put(newHolder, runningAnimInfo);

        return true;
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {
        super.endAnimation(item);
        if (!mAnimatorMap.isEmpty()) {
            final int numRunning = mAnimatorMap.size();
            for (int i = numRunning; i >= 0; i--) {
                if (item == mAnimatorMap.keyAt(i)) {
                    mAnimatorMap.valueAt(i).overallAnim.cancel();
                }
            }
        }
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() || !mAnimatorMap.isEmpty();
    }

    @Override
    public void endAnimations() {
        super.endAnimations();
        if (!mAnimatorMap.isEmpty()) {
            final int numRunning = mAnimatorMap.size();
            for (int i = numRunning; i >= 0; i--) {
                mAnimatorMap.valueAt(i).overallAnim.cancel();
            }
        }
    }

    /**
     * Custom ItemHolderInfo class that holds color and text information used in
     * our custom change animation
     */
    private class NoteInfo extends ItemHolderInfo {
        String time;
        String content;
    }

    /**
     * Holds child animator objects for any change animation. Used when a new change
     * animation interrupts one already in progress; the new one is constructed to start
     * from where the previous one was at when the interruption occurred.
     */
    private class AnimatorInfo {
        Animator overallAnim;
        ObjectAnimator fadeToBlackAnim, fadeFromBlackAnim, oldTimeTextRotator, newTimeTextRotator,
        oldContentTextRotator, newContentTextRotator;

        public AnimatorInfo(Animator overallAnim,
                            ObjectAnimator fadeToBlackAnim, ObjectAnimator fadeFromBlackAnim,
                            ObjectAnimator oldTimeTextRotator, ObjectAnimator newTimeTextRotator,
                            ObjectAnimator oldContentRotator, ObjectAnimator newContentRotator) {
            this.overallAnim = overallAnim;
            this.fadeToBlackAnim = fadeToBlackAnim;
            this.fadeFromBlackAnim = fadeFromBlackAnim;
            this.oldTimeTextRotator = oldTimeTextRotator;
            this.newTimeTextRotator = newTimeTextRotator;
            this.oldContentTextRotator = oldContentRotator;
            this.newContentTextRotator = newContentRotator;
        }
    }
}