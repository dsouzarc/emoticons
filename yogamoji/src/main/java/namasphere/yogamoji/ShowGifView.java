package namasphere.yogamoji;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

public class ShowGifView extends View {

    // Set true to use decodeStream
// Set false to use decodeByteArray
    private static final boolean DECODE_STREAM = true;

    private InputStream gifInputStream;
    private Movie gifMovie;
    private int movieWidth, movieHeight;
    private long movieDuration;
    private long mMovieStart;

    static String gifURL;

    public ShowGifView(Context context, final InputStream io) {
        super(context);
        init(context, io);
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

        gifMovie = null;
        movieWidth = 500;
        movieHeight = 500;
        movieDuration = 0;

        if (DECODE_STREAM) {
            gifMovie = Movie.decodeStream(io);
        } else {
            byte[] array = streamToBytes(io);
            gifMovie = Movie.decodeByteArray(array, 0, array.length);
        }
        this.setMeasuredDimension(1000, 1000);
        movieWidth = gifMovie.width();
        movieHeight = gifMovie.height();

        movieDuration = gifMovie.duration();
    }

    private static byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = is.read(buffer)) >= 0) {
                os.write(buffer, 0, len);
            }
        } catch (java.io.IOException e) {
        }
        return os.toByteArray();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(movieWidth, movieHeight);
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

            gifMovie.setTime(relTime);
            gifMovie.draw(canvas, 0, 0);
            invalidate();

        }

    }
}