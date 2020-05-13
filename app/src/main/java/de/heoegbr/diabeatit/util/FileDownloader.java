package de.heoegbr.diabeatit.util;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.webkit.URLUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.annotation.Nullable;

/**
 * Provides an abstraction of the Android download manager workflow
 */
public class FileDownloader {

  public interface DownloadCallback {

    void onDownloadCompleted(String filePath);
    void onDownloadFailed(Exception error);

  }

	/**
	 * Downloads a file and moves it to the app's file storage folder.
	 *
	 * @param context App context.
	 * @param webUri URI of the file as String.
	 * @param fileName Target filename. If null is supplied, the filename will be guessed.
	 * @param callback Callback to notify caller on completion/failure.
	 */
  public static void download(Context context, String webUri, @Nullable String fileName, DownloadCallback callback) {

		try {

			DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

			long id = dm.enqueue(buildRequest(webUri, fileName));
			context.registerReceiver(buildReceiver(dm, id, callback), new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

		} catch (Exception e) {

			callback.onDownloadFailed(e);

		}

	}

	/**
	 * Builds the download request required by the system download manager.
	 *
	 * @param uri URI of the file as String.
	 * @param fileName Target filename. If null is supplied, the filename will be guessed.
	 * @return Download manager request.
	 */
  private static DownloadManager.Request buildRequest(String uri, String fileName) {

		if (fileName == null)
			fileName = URLUtil.guessFileName(uri, null, null);

		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));
		request.setTitle(fileName);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName == null ? "data" : fileName);

		return request;

  }

	/**
	 * Builds the BroadcastReceiver required for listening for download completion intents.
	 *
	 * @param dm Download Manager instance.
	 * @param id ID of the enqueued request.
	 * @param callback Callback to notify caller on completion/failure.
	 * @return Broadcast receiver that handles file moving and notifying the caller about completion/failure.
	 */
  private static BroadcastReceiver buildReceiver(DownloadManager dm, long id, DownloadCallback callback) {

    return new BroadcastReceiver() {

		  @Override
		  public void onReceive(Context context, Intent intent) {

		    try {

					// Check if our download completed; unregister receiver if necessary
					if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) != id) return;
					context.unregisterReceiver(this);

					// Get status information
			    Cursor c = dm.query(new DownloadManager.Query().setFilterById(id));
			    c.moveToFirst();

			    // Check if download was cancelled
			    if (c.getCount() == 0)
			    	throw new Exception("Cancelled by user");

			    // Check if the download failed
			    if (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {

			      // Return error
				    int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
						throw new Exception((reason < 1000 ? "HTTP Error " : "Internal Error ") + reason);

			    }

			    String filePath = Uri.parse(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))).getPath();
			    c.close();

			    // Successful; move file to internal storage
			    File from = new File(filePath);
			    Path result = Files.move(Paths.get(from.toURI()), Paths.get(new File(context.getFilesDir(), from.getName()).toURI()), StandardCopyOption.REPLACE_EXISTING);

			    callback.onDownloadCompleted(result.toString());

				} catch (Exception e) {

			      callback.onDownloadFailed(e);

				}

		  }

		};

  }

}