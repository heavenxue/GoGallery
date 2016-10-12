package com.aibei.lixue.gogallery.widget2;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;

import com.aibei.lixue.gogallery.R;

import java.lang.reflect.Field;

import static com.aibei.lixue.gogallery.R.styleable.EcoGallery;

public class TosGallery extends TosGalleryAbsSpinner implements GestureDetector.OnGestureListener {

    private static final String TAG = "TosGallery";

    private static final boolean localLOGV = false;

    /**
     * Duration in milliseconds from the start of a scroll during which we're
     * unsure whether the user is scrolling or flinging.
     */
    private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;

    private static final String LOG_TAG = null;

    /**
     * Horizontal spacing between items.
     */
    private int mSpacing = 0;

    /**
     * How long the transition animation should run when a child view changes
     * position, measured in milliseconds.
     */
    private int mAnimationDuration = 200;

    /**
     * The alpha of items that are not selected.
     */
    private float mUnselectedAlpha;

    /**
     * The fling listener.
     */
    private OnEndFlingListener mOnEndFlingListener = null;
    /**
     * Indicate disable scroll action when the child item is less than mItemCount. in other word,
     * the children can be fully seeing in the gallery.
     */
    private boolean mIsDisableScroll = false;

    /**
     * scrolling and animating flag
     */
    private boolean mScrolling = false;


    /**
     * The first child offset.
     */
    private int mFirstChildOffset = 0;


    /**
     * The scroll velocity ratio.
     */
    private float mVelocityRatio = 1.0f;

    /**
     * Indicate the gallery scroll cycle or not.
     */
    private boolean mIsScrollCycle = false;

    /**
     * The temporary member for mIsScrollCycle
     */
    private boolean mIsScrollCycleTemp = true;

    /**
     * Slot into center. The default behavior of gallery is that the selected child will be slot in
     * center.
     */
    private boolean mIsSlotCenter = false;

    /**
     * The orientation horizontal
     */
    public static final int HORIZONTAL = 0x01;

    /**
     * The orientation vertical
     */
    public static final int VERTICAL = 0x02;


    private int mGravity;

    /**
     * Helper for detecting touch gestures.
     */
    private GestureDetector mGestureDetector;

    /**
     * The position of the item that received the user's down touch.
     */
    private int mDownTouchPosition;

    /**
     * The view of the item that received the user's down touch.
     */
    private View mDownTouchView;

    /**
     * Executes the delta scrolls from a fling or scroll movement.
     */
    private FlingRunnable mFlingRunnable = new FlingRunnable();


    /**
     * Sets mSuppressSelectionChanged = false. This is used to set it to false
     * in the future. It will also trigger a selection changed.
     */
    private Runnable mDisableSuppressSelectionChangedRunnable = new Runnable() {
        public void run() {
            mSuppressSelectionChanged = false;
            selectionChanged();
        }
    };

    /**
     * When fling runnable runs, it resets this to false. Any method along the
     * path until the end of its run() can set this to true to abort any
     * remaining fling. For example, if we've reached either the leftmost or
     * rightmost item, we will set this to true.
     */
    private boolean mShouldStopFling;

    /**
     * The currently selected item's child.
     */
    private View mSelectedChild;

    /**
     * Whether to continuously callback on the item selected listener during a
     * fling.
     */
    private boolean mShouldCallbackDuringFling = true;

    /**
     * Whether to callback when an item that is not selected is clicked.
     */
    private boolean mShouldCallbackOnUnselectedItemClick = true;

    /**
     * Left most edge of a child seen so far during layout.
     */
    private int mLeftMost;

    /**
     * Right most edge of a child seen so far during layout.
     */
    private int mRightMost;


    /**
     * If true, do not callback to item selected listener.
     */
    private boolean mSuppressSelectionChanged;

    /**
     * If true, we have received the "invoke" (center or enter buttons) key
     * down. This is checked before we action on the "invoke" key up, and is
     * subsequently cleared.
     */
    private boolean mReceivedInvokeKeyDown;

    private AdapterContextMenuInfo mContextMenuInfo;

    /**
     * If true, this onScroll is the first for this user's drag (remember, a
     * drag sends many onScrolls).
     */
    private boolean mIsFirstScroll;

    /**
     * If true the reflection calls failed and this widget will behave
     * unpredictably if used further
     */
    private boolean mBroken;

    private boolean toLeft;



    public TosGallery(Context context) {
        this(context, null);
    }

    public TosGallery(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.ecoGalleryStyle);
    }

    public TosGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mBroken = true;

        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setIsLongpressEnabled(true);

        TypedArray a = context.obtainStyledAttributes(attrs, EcoGallery, defStyle, 0);

        int index = a.getInt(R.styleable.EcoGallery_gravity, -1);
        if (index >= 0) {
            setGravity(index);
        }

        int animationDuration = a.getInt(R.styleable.EcoGallery_animationDuration, -1);
        if (animationDuration > 0) {
            setAnimationDuration(animationDuration);
        }

        int spacing = a.getDimensionPixelOffset(R.styleable.EcoGallery_spacing, 0);
        setSpacing(spacing);

        float unselectedAlpha = a.getFloat(R.styleable.EcoGallery_unselectedAlpha, 0.5f);
        setUnselectedAlpha(unselectedAlpha);

        mSelectedScale = a.getFloat(R.styleable.EcoGallery_selectedScale, 1.f);

        a.recycle();

        // We draw the selected item last (because otherwise the item to the
        // right overlaps it)
        int FLAG_USE_CHILD_DRAWING_ORDER = 0x400;
        int FLAG_SUPPORT_STATIC_TRANSFORMATIONS = 0x800;
        Class<ViewGroup> vgClass = ViewGroup.class;

        try {
            Field childDrawingOrder = vgClass.getDeclaredField("FLAG_USE_CHILD_DRAWING_ORDER");
            Field supportStaticTrans = vgClass.getDeclaredField("FLAG_SUPPORT_STATIC_TRANSFORMATIONS");

            childDrawingOrder.setAccessible(true);
            supportStaticTrans.setAccessible(true);

            FLAG_USE_CHILD_DRAWING_ORDER = childDrawingOrder.getInt(this);
            FLAG_SUPPORT_STATIC_TRANSFORMATIONS = supportStaticTrans.getInt(this);
        } catch (NoSuchFieldException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        try {
            // set new group flags
            Field groupFlags = vgClass.getDeclaredField("mGroupFlags");
            groupFlags.setAccessible(true);
            int groupFlagsValue = groupFlags.getInt(this);

            groupFlagsValue |= FLAG_USE_CHILD_DRAWING_ORDER;
            groupFlagsValue |= FLAG_SUPPORT_STATIC_TRANSFORMATIONS;

            groupFlags.set(this, groupFlagsValue);

            // working!
            mBroken = false;
        } catch (NoSuchFieldException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    /**
     * @return Whether the widget is broken or working (functional)
     */
    public boolean isBroken() {
        return mBroken;
    }

    /**
     * Whether or not to callback on any {@link #getOnItemSelectedListener()}
     * while the items are being flinged. If false, only the final selected item
     * will cause the callback. If true, all items between the first and the
     * final will cause callbacks.
     *
     * @param shouldCallback Whether or not to callback on the listener while the items are
     *                       being flinged.
     */
    public void setCallbackDuringFling(boolean shouldCallback) {
        mShouldCallbackDuringFling = shouldCallback;
    }

    /**
     * Whether or not to callback when an item that is not selected is clicked.
     * If false, the item will become selected (and re-centered). If true, the
     * {@link #getOnItemClickListener()} will get the callback.
     *
     * @param shouldCallback Whether or not to callback on the listener when a item that is
     *                       not selected is clicked.
     * @hide
     */
    public void setCallbackOnUnselectedItemClick(boolean shouldCallback) {
        mShouldCallbackOnUnselectedItemClick = shouldCallback;
    }

    /**
     * Sets how long the transition animation should run when a child view
     * changes position. Only relevant if animation is turned on.
     *
     * @param animationDurationMillis The duration of the transition, in milliseconds.
     * @attr ref android.R.styleable#Gallery_animationDuration
     */
    public void setAnimationDuration(int animationDurationMillis) {
        mAnimationDuration = animationDurationMillis;
    }

    /**
     * Sets the spacing between items in a Gallery
     *
     * @param spacing The spacing in pixels between items in the Gallery
     * @attr ref android.R.styleable#Gallery_spacing
     */
    public void setSpacing(int spacing) {
        mSpacing = spacing;
    }

    /**
     * Sets the alpha of items that are not selected in the Gallery.
     *
     * @param unselectedAlpha the alpha for the items that are not selected.
     * @attr ref android.R.styleable#Gallery_unselectedAlpha
     */
    public void setUnselectedAlpha(float unselectedAlpha) {
        mUnselectedAlpha = unselectedAlpha;
    }

    @Override
    protected int computeHorizontalScrollExtent() {
        // Only 1 item is considered to be selected
//        return 1;
        // Note: Support the horizontal scroll bar for this gallery.

        final int count = getChildCount();
        if (count > 0) {
            int extent = count * 100;//把总数扩展到100倍

            View view = getChildAt(0);
            final int left = view.getLeft();
            int width = view.getWidth();
            if (width > 0) {
                boolean isFirst = (0 == mFirstPosition);
                // If the first position is zero and the left is more than zero, we do not add the
                // left extent.
                if (!(isFirst && left > 0)) {
                    extent += (left * 100) / width;
                }
            }

            view = getChildAt(count - 1);
            final int right = view.getRight();
            width = view.getWidth();
            if (width > 0) {
                boolean isLast = (mFirstPosition + count == mItemCount);
                // If the last child is show, we do no add the right extent.
                if (!(isLast && right < getWidth())) {
                    extent -= ((right - getWidth()) * 100) / width;
                }
            }

            return extent;
        }

        return 0;
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        // Current scroll position is the same as the selected position
//        return mSelectedPosition;
        if (mFirstPosition >= 0 && getChildCount() > 0) {
            final View view = getChildAt(0);
            final int left = view.getLeft();
            int width = view.getWidth();
            if (width > 0) {
                final int whichCol = mFirstPosition / 1;
                return Math.max(whichCol * 100 - (left * 100) / width, 0);
            }
        }

        return mSelectedPosition;
    }

    @Override
    protected int computeHorizontalScrollRange() {
        // Scroll range is the same as the item count
//        return mItemCount;
        final int numRows = 1;
        final int colCount = (mItemCount + numRows - 1) / numRows;
        return Math.max(colCount * 100, 0);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        /*
         * Gallery expects EcoGallery.LayoutParams.
        */
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        /*
         * Remember that we are in layout to prevent more layout request from
         * being generated.
         */
        mInLayout = true;
        layout(0, false);

        mInLayout = false;
    }

    @Override
    int getChildHeight(View child) {
        return child.getMeasuredHeight();
    }

    /**
     * Tracks a motion scroll. In reality, this is used to do just about any
     * movement to items (touch scroll, arrow-key scroll, set an item as
     * selected).
     *
     * @param deltaX Change in X from the previous event.
     */
    void trackMotionScroll(int deltaX) {

        if (getChildCount() == 0) {
            return;
        }

        toLeft = deltaX < 0;

        if (isSlotInCenter()){
            if (! isScrollCycle() || getChildCount() >= mItemCount){
                int limitedDeltaX = getLimitedMotionScrollAmount(toLeft, deltaX);
                if (limitedDeltaX != deltaX) {
                    // The above call returned a limited amount, so stop any
                    // scrolls/flings
                    mFlingRunnable.endFling(false);
                    onFinishedMovement();
                }
            }


            // 将所有子View 都移动 limiterDeltaX 的距离
            offsetChildrenLeftAndRight(deltaX);

            // 去掉移出屏幕的子View
            detachOffScreenChildren(toLeft);

            // 补充移动后空余位置的视图
            if (toLeft) {
                // If moved left, there will be empty space on the right
                fillToGalleryRight();
            } else {
                // Similarly, empty space on the left
                fillToGalleryLeft();
            }

            //clear used views
            mRecycler.clear();

            //移动过程中会重新修改选中项
            setSelectionToCenterChild();

            invalidate();
            return;
        }

    }

    int getLimitedMotionScrollAmount(boolean motionToLeft, int deltaX) {
        int extremeItemPosition = motionToLeft ? mItemCount - 1 : 0;
        View extremeChild = getChildAt(extremeItemPosition - mFirstPosition);

        if (extremeChild == null) {
            return deltaX;
        }

        int extremeChildCenter = getCenterOfView(extremeChild);
        int galleryCenter = getCenterOfGallery();

        if (motionToLeft) {
            if (extremeChildCenter <= galleryCenter) {

                // The extreme child is past his boundary point!
                return 0;
            }
        } else {
            if (extremeChildCenter >= galleryCenter) {

                // The extreme child is past his boundary point!
                return 0;
            }
        }

        int centerDifference = galleryCenter - extremeChildCenter;

        return motionToLeft ? Math.max(centerDifference, deltaX) : Math.min(centerDifference, deltaX);
    }

    /**
     * Offset the horizontal location of all children of this view by the
     * specified number of pixels.
     *
     * @param offset the number of pixels to offset
     */
    private void offsetChildrenLeftAndRight(int offset) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).offsetLeftAndRight(offset);
        }
    }

    /**
     * @return The center of this Gallery.
     */
    private int getCenterOfGallery() {
        int paddingLeft = getPaddingLeft();
        return (getWidth() - paddingLeft - getPaddingRight()) / 2 + paddingLeft;
    }

    /**
     * @return The center of the given view.
     */
    private static int getCenterOfView(View view) {
        return view.getLeft() + view.getWidth() / 2;
    }

    /**
     * Detaches children that are off the screen (i.e.: Gallery bounds).
     *
     * @param toLeft Whether to detach children to the left of the Gallery, or to
     *               the right.
     */
    private void detachOffScreenChildren(boolean toLeft) {
        int numChildren = getChildCount();
        int firstPosition = mFirstPosition;
        int start = 0;
        int count = 0;

        if (toLeft) {
            final int galleryLeft = getPaddingLeft();
            for (int i = 0; i < numChildren; i++) {
                final View child = getChildAt(i);
                if (child.getRight() >= galleryLeft) {
                    break;
                } else {
                    count++;
                    mRecycler.put(firstPosition + i, child);
                }
            }
            // Do not detach the last child when the child is out of the left bound.
            if (count == numChildren) {
                count -= 1;
            }
        } else {
            final int galleryRight = getWidth() - getPaddingRight();
            for (int i = numChildren - 1; i >= 0; i--) {
                final View child = getChildAt(i);
                if (child.getLeft() <= galleryRight) {
                    break;
                } else {
                    start = i;
                    count++;
                    mRecycler.put(firstPosition + i, child);
                }
            }
            // Do not detach the first child when the child is out of the left bound.
            if (0 == start) {
                start += 1;
            }
        }

        detachViewsFromParent(start, count);

        if (toLeft) {
            mFirstPosition += count;
            Log.d("tt","mFirstPosition:" + mFirstPosition);
            if (isScrollCycle()) {
                mFirstPosition = mFirstPosition % mItemCount;
            }
        }
    }

    /**
     * Scrolls the items so that the selected item is in its 'slot' (its center
     * is the gallery's center).
     */
    private void scrollIntoSlots() {

        if (getChildCount() == 0 || mSelectedChild == null)
            return;
        if (isSlotInCenter()) {
            int selectedCenter = getCenterOfView(mSelectedChild);
            int targetCenter = getCenterOfGallery();

            int scrollAmount = targetCenter - selectedCenter;
            if (scrollAmount != 0) {
                mFlingRunnable.startUsingDistance(scrollAmount);
            } else {
                onFinishedMovement();
            }
            return;
        }
        // Note: Make the gallery item views always dock right or left sides.
        // If the gallery is playing animation, do nothing.
        //
        if (getChildCount() == 0) {
            return;
        }

        int scrollAmount = 0;

        if (0 == mFirstPosition) {
            // In these cases the gallery child count is equal or more than the item count
            // (adapter.getCount()),
            // and the gallery first child's left is bigger than zero, we should move the
            // first child anchors at the most left side of gallery.
            View child = getChildAt(0);

            // Make the first child anchors at the most left side of gallery when it is over
            // the left side of gallery.
            if (child.getLeft() >= 0) {
                scrollAmount = getPaddingLeft() - child.getLeft();
            } else {
                // when scroll from right to left.
                View lastChild = getChildAt(getChildCount() - 1);

                if ((lastChild.getRight() - child.getLeft()) < (getRight() - getPaddingRight())) {
                    scrollAmount = getPaddingLeft() - mFirstChildOffset;
                } else if (lastChild.getRight() < (getRight() - getPaddingRight())) {
                    scrollAmount = getWidth() - getPaddingRight() - lastChild.getRight();
                }
            }
        }
        // If the most right view is the last item.
        else if (mFirstPosition + getChildCount() == mItemCount) {
            View child = getChildAt(getChildCount() - 1);
            // If the child's right side is fully seeing, i.e, the child right side is
            // in the right of gallery.
            if (child.getRight() < (getRight() - getPaddingRight())) {
                scrollAmount = getWidth() - getPaddingRight() - child.getRight();
            }
        }

        if (0 != scrollAmount) {
            // Call startUsingDistance method to implement elastic effect.
            mFlingRunnable.startUsingDistance(scrollAmount);
        } else {
            onFinishedMovement();
        }

    }

    private void onFinishedMovement() {
        if (mSuppressSelectionChanged) {
            mSuppressSelectionChanged = false;

            // We haven't been callbacking during the fling, so do it now
            super.selectionChanged();
        }
        invalidate();
    }

    @Override
    void selectionChanged() {
        if (!mSuppressSelectionChanged) {
            super.selectionChanged();
        }
    }

    /**
     * Looks for the child that is closest to the center and sets it as the
     * selected child.
     */
    private void setSelectionToCenterChild() {

        View selView = mSelectedChild;
        if (mSelectedChild == null)
            return;

        int galleryCenter = getCenterOfGallery();

        // Common case where the current selected position is correct
//        if (selView.getLeft() <= galleryCenter && selView.getRight() >= galleryCenter) {
//            return;
//        }

        // TODO better search
        int closestEdgeDistance = Integer.MAX_VALUE;
        int newSelectedChildIndex = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {

            View child = getChildAt(i);

            if (child.getLeft() <= galleryCenter && child.getRight() >= galleryCenter) {
                // This child is in the center
                newSelectedChildIndex = i;
                break;
            }

            int childClosestEdgeDistance = Math.min(Math.abs(child.getLeft() - galleryCenter),
                    Math.abs(child.getRight() - galleryCenter));
            if (childClosestEdgeDistance < closestEdgeDistance) {
                closestEdgeDistance = childClosestEdgeDistance;
                newSelectedChildIndex = i;
            }
        }
        int newPos = mFirstPosition + newSelectedChildIndex;

        if (isScrollCycle()) {
            newPos = newPos % mItemCount;
        }

        if (newPos != mSelectedPosition) {
            setSelectedPositionInt(newPos);
            setNextSelectedPositionInt(newPos);
            checkSelectionChanged();
        }
    }

    /**
     * Creates and positions all views for this Gallery.
     * <p/>
     * We layout rarely, most of the time {@link #trackMotionScroll(int)} takes
     * care of repositioning, adding, and removing children.
     *
     * @param delta Change in the selected position. +1 means the selection is
     *              moving to the right, so views are scrolling to the left. -1
     *              means the selection is moving to the left.
     */
    @Override
    void layout(int delta, boolean animate) {

        int childrenLeft = mSpinnerPadding.left + mFirstChildOffset;
//        int childrenWidth = getRight() - getLeft() - mSpinnerPadding.left - mSpinnerPadding.right;

        if (mDataChanged) {
            handleDataChanged();
        }

        // Handle an empty gallery by removing all views.
        if (mItemCount == 0) {
            resetList();
            return;
        }

        // Update to the new selected position.
        if (mNextSelectedPosition >= 0) {
            setSelectedPositionInt(mNextSelectedPosition);
        }

        // All views go in recycler while we are in layout
        recycleAllViews();

        // Clear out old views
        detachAllViewsFromParent();

          /*
         * These will be used to give initial positions to views entering the gallery as we scroll
         */
        mRightMost = 0;
        mLeftMost = 0;

        // Make selected view and center it

        /*
         * mFirstPosition will be decreased as we add views to the left later
         * on. The 0 for x will be offset in a couple lines down.
         */
        mFirstPosition = mSelectedPosition;
        View sel = makeAndAddView(mSelectedPosition, 0, 0, true);//得到选中的view

        // Put the selected child in the center
//        int selectedOffset = childrenLeft + (childrenWidth / 2) - (sel.getWidth() / 2);
        int selectedOffset = childrenLeft + mSpacing;
        if (isSlotInCenter()){
            int childrenWidth = getRight() - getLeft() - mSpinnerPadding.left - mSpinnerPadding.right;
            selectedOffset = childrenLeft + (childrenWidth / 2) - (sel.getWidth() / 2);
        }

        sel.offsetLeftAndRight(selectedOffset);

        fillToGalleryRight();
        fillToGalleryLeft();

        invalidate();
//        checkSelectionChanged();

        mDataChanged = false;
        mNeedSync = false;
        setNextSelectedPositionInt(mSelectedPosition);

        updateSelectedItemMetadata();

        // NOTE: If the child count is less than the item count, we should disable cycle scroll,
        // but, we should NOT change the mIsScrollCycle which is set by callers, because
        // after user enlarge the item count such as add data dynamically, if the item count
        // is bigger than child count, the gallery should be scrolling cycle.
        mIsScrollCycleTemp = !(getChildCount() >= mItemCount);
    }

    private void fillToGalleryLeft() {
        if (isSlotInCenter()){
            fillToGalleryLeftCycle();
            return;
        }

        int itemSpacing = mSpacing;
        int galleryLeft = getPaddingLeft();

        // Set state for initial iteration
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curRightEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition - 1;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
        } else {
            // No children available!
            curPosition = 0;
            curRightEdge = getRight() - getLeft() - getPaddingRight();
            mShouldStopFling = true;
        }

        while (curRightEdge > galleryLeft && curPosition >= 0) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curRightEdge, false);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition--;

        }
    }

    /**
     * 手指向左滑动，致使gallery整体向右滑动
     * **/
    private void fillToGalleryRight() {
        if (isScrollCycle()) {
            fillToGalleryRightCycle();
            return;
        }

        int itemSpacing = mSpacing;
        int numChildren = getChildCount();
        int numItems = mItemCount;
        Log.d("ww","numChildren:" + numChildren + ",mFirstPostion:" + mFirstPosition);
        // Set state for initial iteration
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curLeftEdge;
        int galleryRight = getRight() - getLeft() - getPaddingRight();

        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
        } else {
            mFirstPosition = curPosition = mItemCount - 1;
            curLeftEdge = getPaddingLeft();
            mShouldStopFling = true;
        }

        while (curLeftEdge < galleryRight && curPosition < numItems) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curLeftEdge, true);
            Log.d(TAG,"mSelectePosition:" + mSelectedPosition);

            // Set state for next iteration
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition++;
        }
        Log.d(TAG,"curPosition:" + curPosition);
    }

    /**
     * Obtain a view, either by pulling an existing view from the recycler or by
     * getting a new one from the adapter. If we are animating, make sure there
     * is enough information in the view's layout parameters to animate from the
     * old to new positions.
     *
     * @param position Position in the gallery for the view to obtain
     * @param offset   Offset from the selected position
     * @param x        X-coordinate indicating where this view should be placed. This
     *                 will either be the left or right edge of the view, depending
     *                 on the fromLeft parameter
     * @param fromLeft Are we positioning views based on the left edge? (i.e.,
     *                 building from left to right)?
     * @return A view that has been added to the gallery
     */
    private View makeAndAddView(int position, int offset, int x, boolean fromLeft) {

        View child;
        if (!mDataChanged) {
            child = mRecycler.get(position);
            if (child != null) {

                // Can reuse an existing view
                int childLeft = child.getLeft();

                // Remember left and right edges of where views have been placed
                mRightMost = Math.max(mRightMost, childLeft + child.getMeasuredWidth());
                mLeftMost = Math.min(mLeftMost, childLeft);

                // Position the view
                setUpChild(child, offset, x, fromLeft);

                return child;
            }
        }
        // Nothing found in the recycler -- ask the adapter for a view
        child = mAdapter.getView(position, null, this);

        // Position the view
        setUpChild(child, offset, x, fromLeft);

        return child;
    }

    /**
     * Helper for makeAndAddView to set the position of a view and fill out its
     * layout paramters.
     *
     * @param child    The view to position
     * @param offset   Offset from the selected position
     * @param x        X-coordintate indicating where this view should be placed.
     *                 This will either be the left or right edge of the view,
     *                 depending on the fromLeft paramter
     * @param fromLeft Are we posiitoning views based on the left edge? (i.e.,
     *                 building from left to right)?
     */
    private void setUpChild(View child, int offset, int x, boolean fromLeft) {

        // Respect layout params that are already in the view. Otherwise
        // make some up...
        TosGallery.LayoutParams lp = (TosGallery.LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = (TosGallery.LayoutParams) generateDefaultLayoutParams();
        }

        addViewInLayout(child, fromLeft ? -1 : 0, lp);

        child.setSelected(offset == 0);

        // Get measure specs
        int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec, mSpinnerPadding.top + mSpinnerPadding.bottom,
                lp.height);
        int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec, mSpinnerPadding.left + mSpinnerPadding.right,
                lp.width);

        // Measure child
        child.measure(childWidthSpec, childHeightSpec);

        int childLeft;
        int childRight;

        // Position vertically based on gravity setting
        int childTop = calculateTop(child, true);
        int childBottom = childTop + child.getMeasuredHeight();

        int width = child.getMeasuredWidth();
        if (fromLeft) {
            childLeft = x;
            childRight = childLeft + width;
        } else {
            childLeft = x - width;
            childRight = x;
        }

        child.layout(childLeft, childTop, childRight, childBottom);
    }

    /**
     * Figure out vertical placement based on mGravity
     *
     * @param child Child to place
     * @return Where the top of the child should be
     */
    private int calculateTop(View child, boolean duringLayout) {
        int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
        int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight();

        int childTop = 0;

        switch (mGravity) {
            case Gravity.TOP:
                childTop = mSpinnerPadding.top;
                break;
            case Gravity.CENTER_VERTICAL:
                int availableSpace = myHeight - mSpinnerPadding.bottom - mSpinnerPadding.top - childHeight;
                childTop = mSpinnerPadding.top + (availableSpace / 2);
                break;
            case Gravity.BOTTOM:
                childTop = myHeight - mSpinnerPadding.bottom - childHeight;
                break;
        }
        return childTop;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Give everything to the gesture detector
        boolean retValue = mGestureDetector.onTouchEvent(event);

        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            // Helper method for lifted finger
            onUp();
        } else if (action == MotionEvent.ACTION_CANCEL) {
            onCancel();
        }

        return retValue;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onSingleTapUp(MotionEvent e) {

        if (mDownTouchPosition >= 0) {
            if (isScrollCycle()) {
                mDownTouchPosition = mDownTouchPosition % getCount();
            }

            if (isSlotInCenter()){
                // An item tap should make it selected, so scroll to this child.
                scrollToChild(mDownTouchPosition - mFirstPosition);

            }
            performItemSelect(mDownTouchPosition);
            // Also pass the click so the client knows, if it wants to.
            if (mShouldCallbackOnUnselectedItemClick || mDownTouchPosition == mSelectedPosition) {
                performItemClick(mDownTouchView, mDownTouchPosition, mAdapter.getItemId(mDownTouchPosition));
            }

            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * 去除gallery的滚动惯性
     */
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean disableScroll = shouldDisableScroll();

        if (disableScroll) {
            return true;
        }

        //

        if (!mShouldCallbackDuringFling) {
            // We want to suppress selection changes

            // Remove any future code to set mSuppressSelectionChanged = false
            removeCallbacks(mDisableSuppressSelectionChangedRunnable);

            // This will get reset once we scroll into slots
            if (!mSuppressSelectionChanged)
                mSuppressSelectionChanged = true;
        }

        // Accelerate or decelerate the velocity of gallery on X directioin.
        velocityX *= getVelocityRatio();
        // Fling the gallery!
        mFlingRunnable.startUsingVelocity((int) -velocityX);

        return true;
//        if (e1.getX() - e2.getX() < 0.0f){
//            onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT,null);
//        }else{
//            onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT,null);
//        }
//        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        boolean disableScroll = shouldDisableScroll();
        if (localLOGV)
            Log.v(TAG, String.valueOf(e2.getX() - e1.getX()));
        mScrolling = true;
        /*
         * Now's a good time to tell our parent to stop intercepting our events!
         * The user has moved more than the slop amount, since GestureDetector
         * ensures this before calling this method. Also, if a parent is more
         * interested in this touch's events than we are, it would have
         * intercepted them by now (for example, we can assume when a Gallery is
         * in the ListView, a vertical scroll would not end up in this method
         * since a ListView would have intercepted it by now).
         */
        getParent().requestDisallowInterceptTouchEvent(true);

        // As the user scrolls, we want to callback selection changes so
        // related-
        // info on the screen is up-to-date with the gallery's selection
        if (!mShouldCallbackDuringFling) {
            if (mIsFirstScroll) {
                /*
                 * We're not notifying the client of selection changes during
                 * the fling, and this scroll could possibly be a fling. Don't
                 * do selection changes until we're sure it is not a fling.
                 */
                if (!mSuppressSelectionChanged)
                    mSuppressSelectionChanged = true;
                postDelayed(mDisableSuppressSelectionChangedRunnable, SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT);
            }
        } else {
            if (mSuppressSelectionChanged)
                mSuppressSelectionChanged = false;
        }

        // Track the motion
        trackMotionScroll(-1 * (int) distanceX);

        mIsFirstScroll = false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onDown(MotionEvent e) {

        // Kill any existing fling/scroll
        mFlingRunnable.stop(false);

        // Get the item's view that was touched
        mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());

        if (mDownTouchPosition >= 0) {
            mDownTouchView = getChildAt(mDownTouchPosition - mFirstPosition);
            mDownTouchView.setPressed(true);
        }

        // Reset the multiple-scroll tracking state
        mIsFirstScroll = true;

        // Must return true to get matching events for this down event.
        return true;
    }

    /**
     * Called when a touch event's action is MotionEvent.ACTION_UP.
     */
    void onUp() {

        if (mFlingRunnable.mScroller.isFinished()) {
            scrollIntoSlots();
        }

        dispatchUnpress();
    }

    /**
     * Called when a touch event's action is MotionEvent.ACTION_CANCEL.
     */
    void onCancel() {
        onUp();
    }

    /**
     * {@inheritDoc}
     */
    public void onLongPress(MotionEvent e) {

        if (mDownTouchPosition < 0) {
            return;
        }

        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        long id = getItemIdAtPosition(mDownTouchPosition);
        dispatchLongPress(mDownTouchView, mDownTouchPosition, id);
    }

    // Unused methods from GestureDetector.OnGestureListener below

    /**
     * {@inheritDoc}
     */
    public void onShowPress(MotionEvent e) {
    }

    // Unused methods from GestureDetector.OnGestureListener above

    private void dispatchPress(View child) {

        if (child != null) {
            child.setPressed(true);
        }

        setPressed(true);
    }

    private void dispatchUnpress() {

        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).setPressed(false);
        }

        setPressed(false);
    }

    @Override
    public void dispatchSetSelected(boolean selected) {
    /*
     * We don't want to pass the selected state given from its parent to its
     * children since this widget itself has a selected state to give to its
     * children.
     */
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {

        // Show the pressed state on the selected child
        if (mSelectedChild != null) {
            mSelectedChild.setPressed(pressed);
        }
    }

    @Override
    protected ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {

        final int longPressPosition = getPositionForView(originalView);
        if (longPressPosition < 0) {
            return false;
        }

        final long longPressId = mAdapter.getItemId(longPressPosition);
        return dispatchLongPress(originalView, longPressPosition, longPressId);
    }

    @Override
    public boolean showContextMenu() {

        if (isPressed() && mSelectedPosition >= 0) {
            int index = mSelectedPosition - mFirstPosition;
            View v = getChildAt(index);
            return dispatchLongPress(v, mSelectedPosition, mSelectedRowId);
        }

        return false;
    }

    private boolean dispatchLongPress(View view, int position, long id) {
        boolean handled = false;

        if (mOnItemLongClickListener != null) {
            handled = mOnItemLongClickListener.onItemLongClick(this, mDownTouchView, mDownTouchPosition, id);
        }

        if (!handled) {
            mContextMenuInfo = new AdapterContextMenuInfo(view, position, id);
            handled = super.showContextMenuForChild(this);
        }

        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }

        return handled;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Gallery steals all key events
        return event.dispatch(this);
    }

    /**
     * Handles left, right, and clicking
     *
     * @see View#onKeyDown
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            //为了使滑动没有那么有阻力，是效果更好
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (movePrevious()) {
                    playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (moveNext()) {
                    playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                mReceivedInvokeKeyDown = true;
                // fallthrough to default handling
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER: {

                if (mReceivedInvokeKeyDown) {
                    if (mItemCount > 0) {

                        dispatchPress(mSelectedChild);
                        postDelayed(new Runnable() {
                            public void run() {
                                dispatchUnpress();
                            }
                        }, ViewConfiguration.getPressedStateDuration());

                        int selectedIndex = mSelectedPosition - mFirstPosition;
                        performItemClick(getChildAt(selectedIndex), mSelectedPosition, mAdapter.getItemId(mSelectedPosition));
                    }
                }

                // Clear the flag
                mReceivedInvokeKeyDown = false;

                return true;
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    boolean movePrevious() {
        if (mItemCount > 0 && mSelectedPosition >= 0) {
            return true;
        } else {
            return false;
        }
    }

    boolean moveNext() {
        int nextItem;
        if (mItemCount > 0 && mSelectedPosition < mItemCount - 1) {
            return true;
        } else {
            return false;
        }
    }

    private boolean scrollToChild(int childPosition) {
        View child = getChildAt(childPosition);

        if (child != null) {
            int distance = getCenterOfGallery() - getCenterOfView(child);
            mFlingRunnable.startUsingDistance(distance);
            return true;
        }

        return false;
    }

    @Override
    void setSelectedPositionInt(int position) {
        super.setSelectedPositionInt(position);
        Log.d(TAG,"setSelectedPositionInt(),mSelectedPosition: " + mSelectedPosition);
        // Updates any metadata we keep about the selected item.
        updateSelectedItemMetadata();
    }

    private void updateSelectedItemMetadata() {

        View oldSelectedChild = mSelectedChild;
        int index = mSelectedPosition - mFirstPosition;
        if (isScrollCycle()) {
            if (mFirstPosition > mSelectedPosition) {
                index = mItemCount - mFirstPosition + mSelectedPosition;
            }
        }
        View child = mSelectedChild = getChildAt(mSelectedPosition - mFirstPosition);
        if (child == null) {
            return;
        }

        child.setSelected(true);
        child.setFocusable(true);

        if (hasFocus()) {
            child.requestFocus();
        }

        // We unfocus the old child down here so the above hasFocus check
        // returns true
        if (oldSelectedChild != null) {

            // Make sure its drawable state doesn't contain 'selected'
            oldSelectedChild.setSelected(false);

            // Make sure it is not focusable anymore, since otherwise arrow keys
            // can make this one be focused
            oldSelectedChild.setFocusable(false);
        }

    }

    /**
     * Describes how the child views are aligned.
     *
     * @param gravity
     * @attr ref android.R.styleable#Gallery_gravity
     */
    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            requestLayout();
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        int selectedIndex = mSelectedPosition - mFirstPosition;

        // Just to be safe
        if (selectedIndex < 0)
            return i;

        if (i == childCount - 1) {
            // Draw the selected child last
            return selectedIndex;
        } else if (i >= selectedIndex) {
            // Move the children to the right of the selected child earlier one
            return i + 1;
        } else {
            // Keep the children to the left of the selected child the same
            return i;
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

    /*
     * The gallery shows focus by focusing the selected item. So, give focus
     * to our selected item instead. We steal keys from our selected item
     * elsewhere.
     */
        if (gainFocus && mSelectedChild != null) {
            mSelectedChild.requestFocus(direction);
        }

    }

    /**
     * Responsible for fling behavior. Use {@link #startUsingVelocity(int)} to
     * initiate a fling. Each frame of the fling is handled in {@link #run()}. A
     * FlingRunnable will keep re-posting itself until the fling is done.
     */
    private class FlingRunnable implements Runnable {
        /**
         * Tracks the decay of a fling scroll
         */
        private Scroller mScroller;

        /**
         * X value reported by mScroller on the previous fling
         */
        private int mLastFlingX;

        public FlingRunnable() {
            mScroller = new Scroller(getContext());
        }

        private void startCommon() {
            // Remove any pending flings
            removeCallbacks(this);
        }

        public void startUsingVelocity(int initialVelocity) {
            if (initialVelocity == 0)
                return;

            startCommon();

            int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            mLastFlingX = initialX;
            mScroller.fling(initialX, 0, initialVelocity, 0, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            post(this);
        }

        public void startUsingDistance(int distance) {
            if (distance == 0)
                return;

            startCommon();

            mLastFlingX = 0;
            mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration);
            post(this);
        }

        public void stop(boolean scrollIntoSlots) {
            removeCallbacks(this);
            endFling(scrollIntoSlots);
        }

        private void endFling(boolean scrollIntoSlots) {
            mScrolling = false;

            /*
             * Force the scroller's status to finished (without setting its position to the end)
             */
            mScroller.forceFinished(true);

            if (scrollIntoSlots)
                scrollIntoSlots();

            onEndFling();
        }

        public void run() {

            if (mItemCount == 0) {
                endFling(true);
                return;
            }

            mShouldStopFling = false;

            final Scroller scroller = mScroller;
            boolean more = scroller.computeScrollOffset();
            final int x = scroller.getCurrX();

            // Flip sign to convert finger direction to list items direction
            // (e.g. finger moving down means list is moving towards the top)
            int delta = mLastFlingX - x;

            // Pretend that each frame of a fling scroll is a touch scroll
            if (delta > 0) {
                // Moving towards the left. Use first view as mDownTouchPosition
                mDownTouchPosition = mFirstPosition;

                // Don't fling more than 1 screen
                delta = Math.min(getWidth() - getPaddingLeft() - getPaddingRight() - 1, delta);
            } else {
                // Moving towards the right. Use last view as mDownTouchPosition
                int offsetToLast = getChildCount() - 1;
                mDownTouchPosition = mFirstPosition + offsetToLast;

                // Don't fling more than 1 screen
                delta = Math.max(-(getWidth() - getPaddingRight() - getPaddingLeft() - 1), delta);
            }

            trackMotionScroll(delta);

            if (more && !mShouldStopFling) {
                mLastFlingX = x;
                post(this);
            } else {
                endFling(true);
            }
        }

    }

    /**
     * Indicate the gallery selected slot in center of not, default is false.
     *
     * @param isSlotCenter
     */
    public void setSlotInCenter(boolean isSlotCenter) {
        mIsSlotCenter = isSlotCenter;
    }

    /**
     * Indicate the gallery selected slot in center of not, default is false.
     *
     * @return
     */
    public boolean isSlotInCenter() {
        return mIsSlotCenter;
    }

    /**
     * Set the scroll cycle.
     *
     * @param scrollCycle
     */
    public void setScrollCycle(boolean scrollCycle) {
        mIsScrollCycle = scrollCycle;
    }

    /**
     * Get the flag for scroll cycle.
     *
     * @return
     */
    public boolean isScrollCycle() {
        return mIsScrollCycle && mIsScrollCycleTemp;
    }

    /**
     * Select a child.
     */
    private boolean performItemSelect(int childPosition) {
        if (childPosition != mSelectedPosition) {
            setSelectedPositionInt(childPosition);
            setNextSelectedPositionInt(childPosition);
            checkSelectionChanged();

            return true;
        }

        return false;
    }

    /**
     * Indicate the should disable scroll action.
     *
     * @return true if disable scroll, otherwise false.
     *
     * @author LeeHong
     */
    protected boolean shouldDisableScroll() {
        if (mIsDisableScroll) {
            if (getChildCount() < mItemCount) {
                return false;
            }

            // First child is out of gallery left bound.
            View child = getChildAt(0);
            if (null != child && child.getLeft() < getLeft()) {
                return false;
            }

            // Last child is out of gallery right bound.
            child = getChildAt(getChildCount() - 1);
            if (null != child && child.getRight() > getRight()) {
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Called when fling occurs, this method can return ratio value to accelerate or decelerate the
     * velocity of gallery.
     *
     * @return default return 1.0f.
     *
     * @author LeeHong
     */
    public float getVelocityRatio() {
        return mVelocityRatio;
    }
    /**
     * Set the scroll or fling velocity ratio, value is in the range [0.5, 1.5], default value is
     * 1.0f.
     *
     * @param velocityRatio
     *
     * @author LeeHong
     */
    public void setVelocityRatio(float velocityRatio) {
        mVelocityRatio = velocityRatio;

        if (mVelocityRatio < 0.5f) {
            mVelocityRatio = 0.5f;
        } else if (mVelocityRatio > 1.5f) {
            mVelocityRatio = 1.5f;
        }
    }

    private void fillToGalleryLeftCycle() {
        int itemSpacing = mSpacing;
        int galleryLeft = getPaddingLeft();// mPaddingLeft;

        // Set state for initial iteration
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curRightEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition - 1;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
        } else {
            // No children available!
            curPosition = 0;
            // curRightEdge = mRight - mLeft - mPaddingRight;
            curRightEdge = getRight() - getLeft() - getPaddingRight();
            mShouldStopFling = true;
        }

        while (curRightEdge > galleryLeft && curPosition >= 0) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curRightEdge, false);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition--;
        }

        curPosition = mItemCount - 1;
        while (curRightEdge > galleryLeft && getChildCount() < mItemCount) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curRightEdge, false);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition--;
        }
    }

    private void fillToGalleryRightCycle() {
        int itemSpacing = mSpacing;

        // int galleryRight = mRight - mLeft - mPaddingRight;
        int galleryRight = getRight() - getLeft() - getPaddingRight();

        int numChildren = getChildCount();
        int numItems = mItemCount;

        // Set state for initial iteration
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curLeftEdge;

        Log.d(TAG,"  fillToGalleryRightCycle mFirstPosition = " + mFirstPosition);

        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
        } else {
            mFirstPosition = curPosition = mItemCount - 1;
            curLeftEdge = getPaddingLeft();// mPaddingLeft;
            mShouldStopFling = true;
        }

        while (curLeftEdge < galleryRight && curPosition < numItems) {

            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curLeftEdge, true);

            // Set state for next iteration
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition++;
        }

        curPosition = curPosition % numItems;
        while (curLeftEdge <= galleryRight && getChildCount() < mItemCount) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curLeftEdge, true);

            // Set state for next iteration
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition++;
        }
    }

    /**
     * Set the gallery fling listener.
     *
     * @param listener The OnEndFlingListener instance.
     *
     * @author LeeHong
     */
    public void setOnEndFlingListener(OnEndFlingListener listener) {
        mOnEndFlingListener = listener;
    }

    /**
     * Called when the gallery ends fling operation.
     *
     * @author LeeHong
     */
    protected void onEndFling() {
        if (null != mOnEndFlingListener) {
            mOnEndFlingListener.onEndFling(this);
        }
    }

    /**
     * Gallery extends LayoutParams to provide a place to hold current
     * Transformation information along with previous position/transformation
     * info.
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    //
    // ======== 处理Item选中放大 ========
    //

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {

        // 使用drawchild方法设置
        //t.clear();
        //t.setAlpha(child == mSelectedChild ? 1.0f : mUnselectedAlpha);

        return true;
    }

    private float mSelectedScale;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        // 如果设置了选中放大 item 或者透明度变化
        if (mSelectedScale != 1.f || mUnselectedAlpha != 1.f) {
            // 获取 Item 的 View 的宽度和高度
            int childWidth = child.getWidth();
            int childHeight = child.getHeight();

            // 获取 Gallery 中心点坐标的 x 值
            final int center = getCenterOfGallery();

            // 获取 Item 中心点坐标的 x 值
            final int childCenter = child.getLeft() + childWidth / 2;

            // 计算 child 中心点和 Gallery 中心点的距离，并且根据距离计算放大的比例值
            final int offsetCenter = Math.abs(center - childCenter);
            final float offsetScale = (childWidth - offsetCenter) * 1.f / childWidth;

            // 如果 child 和中心点的距离小于 child 的宽度，此时需要放大显示
            if (offsetCenter < childWidth) {
                // 处理 item 的选中放大效果
                if (mSelectedScale != 1.f) {
                    // 设置缩放的中心点坐标
                    child.setPivotX(childWidth / 2.f);
                    child.setPivotY(childHeight - child.getPaddingBottom());

                    // 根据和中心点的距离计算缩放比例
                    float scale = 1 + (mSelectedScale - 1) * offsetScale;
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                }

                // 处理 item 的透明度变化效果
                if (mUnselectedAlpha != 1.f) {
                    float alpha = (1.f - mUnselectedAlpha) * offsetScale + mUnselectedAlpha;
                    child.setAlpha(alpha);
                }
            }
            // 如果 child 和中心点的距离不小于 child 的宽度
            else {
                // 不需要进行缩放
                if (mSelectedScale != 1.f) {
                    child.setScaleX(1.f);
                    child.setScaleY(1.f);
                }

                // 设置未选中的 item 的透明度
                if (mUnselectedAlpha != 1.f) {
                    child.setAlpha(mUnselectedAlpha);
                }
            }
        }

        return super.drawChild(canvas, child, drawingTime);
    }

    /**
     * This interface defines methods for TosGallery.
     *
     * @author LeeHong
     */
    public interface OnEndFlingListener {
        /**
         * Called when the fling operation ends.
         *
         * @param v The gallery view.
         */
        public void onEndFling(TosGallery v);
    }
}