package namasphere.yogamoji;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
public class ShowGifView extends ImageView {

    private static final boolean DECODE_STREAM = true;

    private InputStream gifInputStream;
    private Movie gifMovie;
    private int movieWidth, movieHeight;
    private long movieDuration;
    private long mMovieStart;
    private boolean shouldAnimate = false;
    static String gifURL;

    private String gifName;

    public ShowGifView(Context context, final InputStream io, final String fileName) {
        super(context);
        this.gifName = fileName;
        init(context, io);
    }

    public String getGifName() {
        return this.gifName;
    }

    public ShowGifView(Context context, AttributeSet attrs, final InputStream io) {
        super(context, attrs);
        init(context, io);
    }

    public ShowGifView(Context context, AttributeSet attrs, int defStyleAttr, final InputStream io) {
        super(context, attrs, defStyleAttr);
        init(context, io);
    }

    private void init(final Context context, final InputStream io) {
        setFocusable(true);
        this.gifInputStream = io;
        gifMovie = null;
        movieDuration = 0;

        //Used before when animation happened right away
        /*if (DECODE_STREAM) {
            gifMovie = Movie.decodeStream(io);
        } else {
            byte[] array = streamToBytes(io);
            gifMovie = Movie.decodeByteArray(array, 0, array.length);
        }*/

        byte[] array = streamToBytes(io);

        gifMovie = Movie.decodeByteArray(array, 0, array.length);
        movieWidth = gifMovie.width();
        movieHeight = gifMovie.height();
        movieDuration = gifMovie.duration();
    }

    public void startAnimation() {
        this.shouldAnimate = !this.shouldAnimate;
        invalidate();
    }

    public InputStream getGifInputStream() {
        return this.gifInputStream;
    }

    public static byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = is.read(buffer)) >= 0) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
        }
        return os.toByteArray();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //setMeasuredDimension(movieWidth, movieHeight);
        setMeasuredDimension(500, 500);
    }

    public int getMovieWidth() {
        return movieWidth;
    }

    public int getMovieHeight() {
        return movieHeight;
    }

    public long getMovieDuration() {
        return movieDuration;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) { // first time
            mMovieStart = now;
        }

        if (gifMovie != null) {
            int dur = gifMovie.duration();
            if (dur == 0) {
                dur = 1000;
            }
            int relTime = (int) ((now - mMovieStart) % dur);

            if(shouldAnimate) {
                gifMovie.setTime(relTime);
            }
            else {
                gifMovie.setTime(0);
            }
            gifMovie.draw(canvas, 0, 0);
            invalidate();
        }
    }
}