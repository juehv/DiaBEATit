package de.heoegbr.diabeatit;

/**
 * Collection of constants and shared variables
 */
public class StaticData {
	/* ID of the foreground service */
	public static final int FOREGROUND_SERVICE_ID = 1;
	/* ID of the intent generated to restart the app */
	public static final int RESTART_INTENT_ID = 999;

	/* Intent code that is used to open the assistant */
	public static final String ASSISTANT_INTENT_CODE = "info.nightscout.androidaps.OPEN_ASSISTANT";

	/* URL pointing to the user handbook */
	public static final String HANDBOOK_URL = "https://diabeatit.de/app-docs/";
	/* Contect mail address */
	public static final String CONTACT_MAIL = "mailto:diabeatit@tk.tu-darmstadt.de";
	/* Error report mailto */
	public static final String ERROR_MAIL = CONTACT_MAIL + "?subject=Stacktrace&body=%s";

	/* Name of the database managed by Room */
	public static final String ROOM_DATABASE_NAME = "diabeatit";

	/* Stabilizes the closing behaviour of the Assistant */
	public static boolean assistantInhibitClose = false;

}