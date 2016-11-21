package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;

import java.text.MessageFormat;
import java.util.Random;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;
    private Canvas _offScreenCanvas = null;
    public Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();

    private int _alpha = 150;
    private int _defaultRadius = 25;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;


    // added
    private Bitmap _imageViewBitmap;
    private Rect _imageViewRect;        // the rectangle coordinates to check if valid
    private VelocityTracker _velocityTracker = null;


    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));



    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
        _imageViewBitmap = _imageView.getDrawingCache();
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){

        _brushType = brushType;

    }

    /**
     * Clears the painting
     */
    public void clearPainting(){

        if(_offScreenCanvas != null) {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            _offScreenCanvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
            invalidate();   // force the onDraw method to be called
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);

        _imageViewBitmap = _imageView.getDrawingCache();

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        //TODO
        //Basically, the way this works is to liste for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location
        float curTouchX = motionEvent.getX();
        float curTouchY = motionEvent.getY();
        int curTouchXRounded = (int) curTouchX;
        int curTouchYRounded = (int) curTouchY;

        float brushRadius = _defaultRadius;

        _imageViewRect = getBitmapPositionInsideImageView(_imageView);





        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(_velocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    _velocityTracker = VelocityTracker.obtain();
                } else {
                    // Reset the velocity tracker back to its initial state.
                    _velocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                _velocityTracker.addMovement(motionEvent);
            case MotionEvent.ACTION_MOVE:
                _velocityTracker.addMovement(motionEvent);
                // When you want to determine the velocity, call
                // computeCurrentVelocity(). Then call getXVelocity()
                // and getYVelocity() to retrieve the velocity for each pointer ID.
                _velocityTracker.computeCurrentVelocity(1000);  // 1000 for pixels per second

                int index = motionEvent.getActionIndex();
                int pointerId = motionEvent.getPointerId(index);
                float xVelocity = VelocityTrackerCompat.getXVelocity(_velocityTracker, pointerId);
                float yVelocity = VelocityTrackerCompat.getYVelocity(_velocityTracker, pointerId);
                double realVelocity = Math.sqrt(Math.pow(xVelocity, 2) + Math.pow(yVelocity, 2));
                // velocity to brush size
                Log.d("VELOCITY", "Velocity = " + realVelocity);

                // multiply the ratio of 50 and 3000. 50 is the max brush size and 3000 is the max velocity
                float velocityBrushSize = Math.round((realVelocity * 30) / 4000);

                int pixel = Color.WHITE;
                if (_imageViewRect.contains(curTouchXRounded, curTouchYRounded)) {
                    pixel = _imageViewBitmap.getPixel(curTouchXRounded, curTouchYRounded);    // crashes when off screen
                }
                _paint.setColor(pixel);
                _paint.setStrokeWidth(brushRadius);

                for (int i = 0; i < motionEvent.getHistorySize(); i++) {
                    float touchX = motionEvent.getHistoricalX(i);
                    float touchY = motionEvent.getHistoricalY(i);
                    drawOnCanvas(touchX, touchY, velocityBrushSize);

                }
                drawOnCanvas(curTouchX, curTouchY, velocityBrushSize);


            case MotionEvent.ACTION_UP:
        }

        invalidate();
        return true;
    }


    public void drawOnCanvas(float x, float y, float speedBrushSize) {
        switch (_brushType) {

            case Circle:
                _offScreenCanvas.drawCircle(x, y, speedBrushSize, _paint);
                break;
            case Square:
                _offScreenCanvas.drawRect(x - speedBrushSize, y - speedBrushSize, x + speedBrushSize, y + speedBrushSize, _paint);
                break;
            case Line:
                _offScreenCanvas.drawLine(x, y, x+80f , y+80f, _paint);
            case CircleSplatter:
                Random rand = new Random();
                for (int i = 0; i < 10; i++) {
                    double num = (rand.nextDouble() * 2 - 1)*10;
                    float xRes = (float) (x + num);
                    float yRes = (float) (y + num);
                    _offScreenCanvas.drawCircle(xRes, yRes, speedBrushSize, _paint);
                }
                break;

            case LineSplatter:
                Random random = new Random();
                for (int i = 0; i < 10; i++) {
                    double num = (random.nextDouble() * 2 - 1)*10;
                    float xRes = (float) (x + num);
                    float yRes = (float) (y + num);
                    _offScreenCanvas.drawLine(xRes, yRes, xRes+80f, yRes+80f, _paint);
                }

                break;
            case ParticleEmitter:
//                http://stackoverflow.com/questions/26167972/drawing-random-circles-in-random-locations-in-random-sizes-android
                int minRadius = 5;
                Random random1 = new Random();
                int w = getWidth();
                int h = getHeight();
                int randX = random1.nextInt(w);
                int randY = random1.nextInt(h);
                int randR = minRadius + random1.nextInt(10);
                _offScreenCanvas.drawCircle(randX, randY, randR, _paint);

                break;

        }


    }



    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }




}

