package de.heoegbr.diabeatit.data.container.event;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.room.Entity;
import androidx.room.Ignore;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Locale;

import de.heoegbr.diabeatit.R;

/**
 * Event that represents a meal
 */
@Entity
public class MealEvent extends DiaryEvent {
    private static final String TAG = "MEAL_EVENT";

    /**
     * Create a new carbs event
     *
     * @param timestamp   Timestamp when the meal was taken
     * @param picturePath Path to image of the meal (Optional)
     * @param carbs       Amount of carbs in grams
     * @param note        Optional note
     */
    @Ignore
    public MealEvent(@Source int source, Instant timestamp, String picturePath, double carbs, String note) {
        super(TYPE_MEAL, source, R.drawable.ic_fab_carbs, timestamp, carbs, picturePath, note);
    }

    /**
     * Create a new carbs event. This constructor is mainly used to generate an object from the
     * database.
     *
     * @param logEventId  Unique ID of this object, used as primary key and auto-generated
     * @param iconId      Resource ID of an icon that may be displayed for this event
     * @param timestamp   Timestamp when the meal was taken
     * @param picturePath Optional path to an image of the meal
     * @param value       Amount of carbs in grams
     * @param note        Optional note
     */
    public MealEvent(@Source int source, long logEventId, int iconId, Instant timestamp,
                     String picturePath, double value, String note) {
        super(TYPE_MEAL, source, logEventId, iconId, timestamp, value, picturePath, note);
    }

    //TODO move to utility class (including in bolus calculator)
    private static Bitmap rotateImage(Bitmap source, float angle) {
        if (angle == 0) return source;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @Override
    public void createLayout(Context context, RelativeLayout root, boolean isSelected) {
        TextView titleV = root.findViewById(R.id.log_event_title);
        ImageView iconV = root.findViewById(R.id.log_event_icon);
        TextView timeV = root.findViewById(R.id.log_event_time);
        TextView contentV = root.findViewById(R.id.log_event_content);
        TextView noteV = root.findViewById(R.id.log_event_note);
        ImageView imgV = root.findViewById(R.id.log_event_picture);

        titleV.setText(context.getResources().getString(R.string.mc_event_title));
        iconV.setImageResource(iconId);
        timeV.setText(new SimpleDateFormat("dd.MM.YYYY HH:mm", Locale.GERMAN)
                .format(Date.from(timestamp)));

        contentV.setVisibility(View.VISIBLE);
        noteV.setVisibility(!note.isEmpty() ? View.VISIBLE : View.GONE);

        root.setBackgroundResource(isSelected ?
                R.drawable.log_event_selected_background :
                R.drawable.log_event_background);

        contentV.setText(value + "g");
        if (note == null)
            noteV.setVisibility(View.GONE);
        else {
            noteV.setVisibility(!note.isEmpty() ? View.VISIBLE : View.GONE);
            noteV.setText(note);
        }

        if (picturePath != null && !picturePath.isEmpty()) {
            try {
                imgV.setVisibility(View.VISIBLE);
                setPic(imgV, picturePath);
            } catch (IOException ignored) {
                Log.e(TAG, ignored.getMessage());
            }
        } else {
            imgV.setVisibility(View.GONE);
        }
    }

    //TODO move to utility class (including in bolus calculator)
    private void setPic(ImageView imageView, String imagePath) throws IOException {
        // TODO revisit picture orientation issue when time
        // https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a

        ExifInterface ei = new ExifInterface(imagePath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        float rotation = 0;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
        }

        // Get the dimensions of the View
        int targetW;
        int targetH;
        if (rotation == 0 || rotation == 180) {
            targetW = imageView.getWidth();
            targetH = imageView.getHeight();
        } else {
            targetW = imageView.getHeight();
            targetH = imageView.getWidth();
        }

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = 5;// Math.max(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
//        cameraPreview.setImageBitmap(rotateImage(bitmap, rotation));
//        cameraPreviewContainer.setVisibility(View.GONE);
        imageView.setImageBitmap(rotateImage(bitmap, rotation));
    }

}