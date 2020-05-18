package de.heoegbr.diabeatit.interfacing.xdrip;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.format.ISODateTimeFormat;

import java.time.Instant;

import de.heoegbr.diabeatit.db.container.event.BgReadingEvent;
import de.heoegbr.diabeatit.db.container.event.DiaryEvent;

public class NsEntry {
    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("device")
    @Expose
    public String device;
    @SerializedName("date")
    @Expose
    public Long date;
    @SerializedName("dateString")
    @Expose
    public String dateString;
    @SerializedName("sgv")
    @Expose
    public Double sgv;
    @SerializedName("delta")
    @Expose
    public Double delta;
    @SerializedName("direction")
    @Expose
    public String direction;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("sysTime")
    @Expose
    public String sysTime;
    @SerializedName("utcOffset")
    @Expose
    public Long utcOffset;

    public BgReadingEvent toBgReadingEvent(@DiaryEvent.Source int source) {
        return new BgReadingEvent(source,
                Instant.ofEpochMilli(ISODateTimeFormat.dateTimeParser().parseDateTime(dateString).getMillis()),
                sgv,
                "");
    }
}
