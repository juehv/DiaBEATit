package de.heoegbr.diabeatit.db.container;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.ui.NotificationHelper;

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
    public void send() {
        if (!notify || !active) return;

        destroy();
        notification_id = NotificationHelper.sendNotification(urgency.getChannel(), this);
    }

    /**
     * Destroy any associated Android notification.
     */
    public void destroy() {
        if (notification_id < 0) return;

        NotificationHelper.removeNotification(notification_id);
        notification_id = -1;
    }

    /*
     * The Urgency dictates the design and position of the Alert card, as well as effects like
     * notification and ringtone. It has a comparable priority attribute (higher value -> higher
     * urgency) and the ID of the label drawable.
     */
    public enum URGENCY {

        INFO(1, R.string.alert_label_info, R.drawable.label_gray, R.color.d_info, "info", "General Alerts", android.app.NotificationManager.IMPORTANCE_DEFAULT, R.string.assistant_peek_title_info),
        WARNING(2, R.string.alert_label_warning, R.drawable.label_amber, R.color.d_warning, "warning", "Warnings", android.app.NotificationManager.IMPORTANCE_HIGH, R.string.assistant_peek_title_warning),
        URGENT(3, R.string.alert_label_urgent, R.drawable.label_red, R.color.d_important, "important", "Important Alerts", android.app.NotificationManager.IMPORTANCE_HIGH, R.string.assistant_peek_title_urgent);

        private int priority, stringId, background, rawColor, peekTitle;
        private String channel;

        URGENCY(int p, int s, int b, int r, String nId, String nTitle, int nImp, int t) {
            priority = p;
            stringId = s;
            background = b;
            rawColor = r;
            channel = nId;
            peekTitle = t;

            NotificationHelper.createChannel(nId, nTitle, nTitle, nImp);
        }

        public int getPriority() {
            return priority;
        }

        public int getStringId() {
            return stringId;
        }

        public int getBackground() {
            return background;
        }

        public int getRawColor() {
            return rawColor;
        }

        public String getChannel() {
            return channel;
        }

        public int getPeekTitle() {
            return peekTitle;
        }

    }

}