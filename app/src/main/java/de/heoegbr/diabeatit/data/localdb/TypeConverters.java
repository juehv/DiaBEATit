package de.heoegbr.diabeatit.data.localdb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.TypeConverter;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Date;

import de.heoegbr.diabeatit.data.container.Alert;

/**
 * Collection of methods to convert types from and to objects that Room knows how to store in the
 * database
 */
public class TypeConverters {
    /** Convert a {@link Long} representing the milliseconds since the UNIX Epoch into a {@link Date}
     * object.
     *
     * @param value     Amount of milliseconds since the UNIX Epoch
     * @return          A Date representing the date encoded in the {@code value}. If the provided
     *                  {@code value} is {@code null} the result will also be {@code null}
     */
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    /** Convert a {@link Date} into a {@link Long} representing the amount of milliseconds since the
     * UNIX Epoch.
     *
     * @param date      The {@link Date} to be converted
     * @return          Amount of milliseconds since the UNIX Epoch for the timestamp represented
     *                  by the {@link Date} object provided.
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    /** Convert an {@link Alert.URGENCY} into
     * an {@link Integer} representing its ordinal
     *
     * @param value     Urgency to convert
     * @return          An {@link Integer} representing the ordinal of the {@code value} provided
     */
    @TypeConverter
    public static Integer urgencyToInteger(Alert.URGENCY value) {
        return value == null ? null : value.ordinal();
    }

    /** Convert an {@link Integer} representing the ordinal of an {@link Alert.URGENCY}
     * into the {@link Alert.URGENCY} object
     *
     * @param value     An {@link Integer} representing the ordinal of an {@link Alert.URGENCY}
     *                  value
     * @return An {@link Alert.URGENCY}
     *                  object with the given ordinal
     */
    @TypeConverter
    public static Alert.URGENCY integerToUrgency(Integer value) {
        return value == null ? null : Alert.URGENCY.values()[value];
    }

    /** Convert an {@link Instant} into a {@link Long} representing the amount of milliseconds since
     * the UNIX Epoch
     *
     * @param value An {@link Instant} that should be converted
     * @return      Amount of milliseconds since the UNIX Epoch for the timestamp represented by the
     *              given {@link Instant}
     */
    @TypeConverter
    public static Long instantToTimestamp(Instant value) {
        return value == null ? null : value.toEpochMilli();
    }

    /** Convert a {@link Long} representing the amount of milliseconds since the UNIX Epoch into an
     * appropiate {@link Instant} object.
     *
     * @param value     A {@link Long} representing the amount of milliseconds since the UNIX Epoch
     * @return          An {@link Instant} object representing the appropriate point in time.
     */
    @TypeConverter
    public static Instant timestampToInstant(Long value) {
        return value == null ? null : Instant.ofEpochMilli(value);
    }

    /** Convert a {@link Bitmap} into an {@link Byte}-Array.
     * This encodes and compresses the {@link Bitmap} as PNG.
     *
     * The given Bitmap will not be marked as recycled.
     *
     * @param value     A {@link Bitmap} to convert
     * @return          A {@link Byte}-Array containing the binary data of the bitmap, encoded and
     *                  compress as PNG.
     */
    @TypeConverter
    public static byte[] bitmapToBlob(Bitmap value) {
        if (value == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        value.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    /** Convert a {@link Byte}-Array into a {@link Bitmap}
     *
     * @param value     Buffer containing the Image data.
     * @return          A {@link Bitmap} decoded of the provided data.
     */
    @TypeConverter
    public static Bitmap blobToBitmap(byte[] value) {
        if (value == null) return null;

        Bitmap bmp = BitmapFactory.decodeByteArray(value, 0, value.length);
        return bmp;
    }
}
