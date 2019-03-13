package com.example.bezierCurve;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.io.InputStream;

public class MyActivity extends Activity {
    private Pager pager;
    private PagerFactory pagerFactory;
    private Bitmap currentBitmap, mCurPageBitmap, mNextPageBitmap;
    private Canvas mCurPageCanvas, mNextPageCanvas;
    private static final String[] pages = {"one","two"};
    private int screenWidth;
    private int screenHeight;
    private Bitmap mYbitmap;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View view = getLayoutInflater()
                .inflate(R.layout.main,null);
        setContentView(view);

        final View view1 = findViewById(R.id.draw_view);

        view1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean isDraw = false;
            @Override
            public void onGlobalLayout() {
                if(!isDraw) {
                    mYbitmap = getBitMap(view1);
                    view.setVisibility(View.VISIBLE);

                    initView(mYbitmap);
                    isDraw = true;
                }

            }
        });


        view.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        },1000);

    }

    private void initView(Bitmap bitmap) {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        pager = new Pager(this, screenWidth, screenHeight);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(pager, layoutParams);

        mCurPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        mNextPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);

        mCurPageCanvas = new Canvas(mCurPageBitmap);
        mNextPageCanvas = new Canvas(mNextPageBitmap);
        pagerFactory = new PagerFactory(getApplicationContext());

        pager.setBitmaps(mCurPageBitmap, mCurPageBitmap);
        loadImage(mCurPageCanvas, 0);
        loadImage(mNextPageCanvas, 1);

        pager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                boolean ret = false;
                if (v == pager) {
                    if (e.getAction() == MotionEvent.ACTION_DOWN) {
                        startAnimation(v,pager);
                    }
                    return ret;
                }
                return false;
            }
        });
    }

    private static final int TIME = 4000;
    private static final int WATING = 1000;
    /**
     * 开始动画
     */
    public void startAnimation(final View view, final Pager pager){



        PointF start = new PointF(view.getRight(),view.getBottom());
        PointF end = new PointF(-view.getRight(),-view.getBottom());

        pager.calcCornerXY(start.x, start.y);

        ValueAnimator valueAnimator = ValueAnimator.ofObject(new TypeEvaluator<PointF>() {
            @Override
            public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
                float x = startValue.x - ((startValue.x + Math.abs(endValue.x))*fraction);
                float y= startValue.y -((startValue.y + Math.abs(endValue.y)) * fraction);
                return new PointF(x,y);
            }
        },start,end);
        valueAnimator.setDuration(TIME);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF pointF = (PointF) animation.getAnimatedValue();
                pager.doTouchXY(pointF.x,pointF.y);
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        endAnimation(view,pager);
                    }
                },WATING);
            }
        });
        valueAnimator.start();
    }

    /**
     * 开始动画
     */
    public void endAnimation(View view, final Pager pager){
        PointF start = new PointF(-view.getRight(),-view.getBottom());
        PointF end = new PointF(view.getRight(),view.getBottom());

        ValueAnimator valueAnimator = ValueAnimator.ofObject(new TypeEvaluator<PointF>() {
            @Override
            public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
                float x = startValue.x + ((Math.abs(startValue.x) + endValue.x) * fraction);
                float y = startValue.y + ((Math.abs(startValue.y) + endValue.y) * fraction);
                return new PointF(x,y);
            }
        },start,end);
        valueAnimator.setDuration(TIME);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF pointF = (PointF) animation.getAnimatedValue();
                pager.doTouchXY(pointF.x,pointF.y);
            }
        });
        valueAnimator.start();
    }



    private void loadImage(final Canvas canvas, int index) {
        Bitmap bitmap = index == 0? mYbitmap:getBitmap(pages[index]);
//        currentBitmap = bitmap;
        pagerFactory.onDraw(canvas, bitmap);
        pager.setBitmaps(mCurPageBitmap, mNextPageBitmap);
        pager.postInvalidate();
    }

    private Bitmap getBitmap(String name) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        ApplicationInfo appInfo = getApplicationInfo();
        int resID = getResources().getIdentifier(name, "drawable", appInfo.packageName);
        InputStream is = getResources().openRawResource(resID);
        Bitmap tempBitmap = BitmapFactory.decodeStream(is, null, opt);
        int width = tempBitmap.getWidth();
        int height = tempBitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(((float)screenWidth)/width, ((float)screenHeight)/height);
        Bitmap bitmap = Bitmap.createBitmap(tempBitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }


    private Bitmap getBitMap(View view){
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),view.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas  = new Canvas(bitmap);
        view.layout(view.getLeft(),view.getTop(),view.getRight(),view.getBottom());
        view.draw(canvas);
        return bitmap;
    }
}
