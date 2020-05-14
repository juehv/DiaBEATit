package de.heoegbr.diabeatit.db.container;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.Html;

import androidx.core.app.NotificationCompat;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Random;

import de.heoegbr.diabeatit.DiaBEATitApp;
import de.heoegbr.diabeatit.R;

@Entity
public class Alert {

    @ColumnInfo(name = "urgency")
    public final URGENCY urgency;
    @ColumnInfo(name = "icon_id")
    public final int iconId;
    @PrimaryKey(autoGenerate = true)
    public long alertId;
    @ColumnInfo(name = "title")
    public String title;
    @ColumnInfo(name = "description")
    public String description;
    @ColumnInfo(name = "timestamp")
    public Date timestamp;
    @ColumnInfo(name = "active")
    public boolean active = true;
    @ColumnInfo(name = "notify")
    public boolean notify = true;
    @Ignore
    private int notification_id = -1;

    public Alert(URGENCY urgency, int iconId, String title, String descriptionHtml) {
        this(urgency, iconId, title, descriptionHtml, new Date());
    }

    public Alert(URGENCY urgency, int iconId, String title, String descriptionHtml, Date creation) {
        this.urgency = urgency;
        this.iconId = iconId;
        this.title = title;
        this.description = descriptionHtml;
        this.timestamp = creation;
    }

    public Alert(long alertId, URGENCY urgency, int iconId, String title, String description, Date timestamp, boolean active, boolean notify) {
        this(urgency, iconId, title, description, timestamp);
        this.alertId = alertId;
        this.active = active;
        this.notify = notify;
    }

    /**
     * Sends an Android notification associated with this Alert. Any existing notification will be replaced.
     */
    public void send(Context context) {
        if (!notify || !active) return;

        destroy(context);
        // TODO do real id generation based on the effect of this id (TODO find out the effect of the id)
        notification_id = Math.abs(new Random().nextInt());

        Intent notificationIntent = new Intent(context, DiaBEATitApp.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, notificationIntent, 0);
        Notification notification =
                new NotificationCompat.Builder(context, DiaBEATitApp.DEFAULT_NOTIFICAITON_CHANNEL_ID)
                        .setSmallIcon(iconId)
                        .setContentTitle(title)
                        .setContentText(Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT))
                        .setContentIntent(pendingIntent)
                        .build();
        context.getSystemService(NotificationManager.class).notify(notification_id, notification);
    }

    /**
     * Destroy any associated Android notification.
     */
    public void destroy(Context context) {
        if (notification_id < 0) return;

        context.getSystemService(NotificationManager.class)
                .cancel(notification_id);
        notification_id = -1;
    }

    /*
     * The Urgency dictates the design and position of the Alert card, as well as effects like
     * notification and ringtone. It has a comparable priority attribute (higher value -> higher
     * urgency) and the ID of the label drawable.
     */
    public enum URGENCY {

        INFO(1, R.string.alert_label_info, R.drawable.label_gray, R.color.d_info, R.string.assistant_peek_title_info),
        WARNING(2, R.string.alert_label_warning, R.drawable.label_amber, R.color.d_warning, R.string.assistant_peek_title_warning),
        URGENT(3, R.string.alert_label_urgent, R.drawable.label_red, R.color.d_important, R.string.assistant_peek_title_urgent);

        private int priority, stringId, bgStyle, rawColor, peekTitle;

        URGENCY(int priority, int stringId, int bgStyle, int rawColor, int peekTitle) {
            this.priority = priority;
            this.stringId = stringId;
            this.bgStyle = bgStyle;
            this.rawColor = rawColor;
            this.peekTitle = peekTitle;
        }

        public int getPriority() {
            return priority;
        }

        public int getStringId() {
            return stringId;
        }

        public int getBgStyle() {
            return bgStyle;
        }

        public int getRawColor() {
            return rawColor;
        }

        public int getPeekTitle() {
            return peekTitle;
        }
    }

}