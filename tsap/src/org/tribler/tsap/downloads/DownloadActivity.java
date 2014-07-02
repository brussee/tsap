package org.tribler.tsap.downloads;


import java.io.File;
import java.io.FilenameFilter;

import org.tribler.tsap.R;
import org.tribler.tsap.settings.Settings;
import org.tribler.tsap.streaming.PlayButtonListener;
import org.tribler.tsap.util.MainThreadPoller;
import org.tribler.tsap.util.Poller.IPollListener;
import org.tribler.tsap.util.ThumbnailUtils;
import org.tribler.tsap.util.Utility;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadActivity extends Activity implements IPollListener {
	private ActionBar mActionBar;
	private Download mDownload;
	private View mView;
	private MainThreadPoller mPoller;

	public final static String INTENT_MESSAGE = "org.tribler.tsap.DownloadActivity.IntentMessage";

	/**
	 * Sets the desired options in the action bar
	 * 
	 * @param title
	 *            The title to be displayed in the action bar
	 */
	private void setupActionBar(String title) {
		mActionBar = getActionBar();
		mActionBar.setTitle(title);
		mActionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void fillLayout() {
		TextView size = (TextView) mView
				.findViewById(R.id.download_info_filesize);
		size.setText(Utility.convertBytesToString(mDownload.getSize()));

		TextView download = (TextView) mView
				.findViewById(R.id.download_info_down_speed);
		download.setText(Utility.convertBytesPerSecToString(mDownload
				.getDownloadSpeed()));

		TextView upload = (TextView) mView
				.findViewById(R.id.download_info_up_speed);
		upload.setText(Utility.convertBytesPerSecToString(mDownload
				.getUploadSpeed()));

		TextView availability = (TextView) mView
				.findViewById(R.id.download_info_availability);
		availability.setText(Integer.toString(mDownload.getAvailability()));

		TextView descr = (TextView) mView
				.findViewById(R.id.download_info_description);
		descr.setText("");

		ImageView thumb = (ImageView) mView
				.findViewById(R.id.download_info_thumbnail);
		ThumbnailUtils.loadThumbnail(getImageLocation(mDownload.getInfoHash()), thumb, this);

		TextView status = (TextView) mView
				.findViewById(R.id.download_info_status_text);
		status.setText(Utility.convertDownloadStateIntToMessage(mDownload
				.getStatus())
				+ ((mDownload.getStatus() == 2 || mDownload.getStatus() == 3) ? " ("
						+ Math.round(mDownload.getProgress() * 100) + "%)"
						: ""));

		TextView eta = (TextView) mView
				.findViewById(R.id.download_info_eta_text);
		eta.setText((mDownload.getStatus() == 3) ? Utility
				.convertSecondsToString(mDownload.getETA()) : "Unknown");

		ProgressBar bar = (ProgressBar) mView
				.findViewById(R.id.download_info_progress_bar);
		bar.setProgress((int) (100 * mDownload.getProgress()));
	}

	private void setStreamButtonListener() {
		Button streamButton = (Button) mView
				.findViewById(R.id.download_info_stream_button);
		View.OnClickListener streamButtonOnClickListener = new PlayButtonListener(
				mDownload.getInfoHash(), getFragmentManager(), this);
		streamButton.setOnClickListener(streamButtonOnClickListener);
	}

	private void setTorrentRemoveButtonListener(int resource) {
		Button removeButton = (Button) mView.findViewById(resource);
		final DownloadActivity a = this;

		View.OnClickListener removeButtonOnClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show dialog
				AlertDialog.Builder alertRemove = new AlertDialog.Builder(
						v.getContext());
				alertRemove.setTitle(R.string.remove_download_dialog_title)
						.setMessage(R.string.remove_download_dialog_message)
						// Android.R.string.yes == Ok -
						// https://code.google.com/p/android/issues/detail?id=3713
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										XMLRPCDownloadManager
												.getInstance()
												.deleteTorrent(
														mDownload.getInfoHash(),
														true);
										a.onBackPressed();
									}
								})
						// Android.R.string.no == Cancel -
						// https://code.google.com/p/android/issues/detail?id=3713
						.setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										XMLRPCDownloadManager
												.getInstance()
												.deleteTorrent(
														mDownload.getInfoHash(),
														false);
										a.onBackPressed();
									}
								})
						// .setIcon(android.R.drawable.ic_dialog_alert)
						.setNeutralButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();
			}
		};
		removeButton.setOnClickListener(removeButtonOnClickListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView = getWindow().getDecorView().getRootView();
		setContentView(R.layout.activity_download);

		Intent intent = getIntent();
		mDownload = (Download) intent.getSerializableExtra(INTENT_MESSAGE);

		setupActionBar(mDownload.getName());
		fillLayout();
		setStreamButtonListener();
		setTorrentRemoveButtonListener(R.id.download_info_delete_torrent_button);

		mPoller = new MainThreadPoller(this, 2000, this);
		mPoller.start();
	}

	/**
	 * Pauses polling
	 */
	@Override
	public void onPause() {
		super.onPause();
		mPoller.stop();
	}

	/**
	 * Resumes the poller
	 */
	@Override
	public void onResume() {
		super.onResume();
		mPoller.start();
	}

	/**
	 * Called when one of the icons in the start bar is tapped: When the home
	 * icon is tapped, go back. If any other icon is tapped do the default
	 * action.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if (menuItem.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		} else {
			return super.onOptionsItemSelected(menuItem);
		}
	}

	private File getImageLocation(final String infoHash) {
		File baseDirectory = Settings.getThumbFolder();
		if (baseDirectory == null || !baseDirectory.isDirectory()) {
			Log.e("DownloadInfo",
					"The collected_torrent_files thumbnailfolder could not be found");
			return null;
		}

		File thumbsDirectory = new File(baseDirectory, "thumbs-" + infoHash);
		if (!thumbsDirectory.exists()) {
			Log.d("DownloadInfo", "No thumbnailfolder found for " + infoHash);
			return null;
		}

		File thumbsSubDirectory = null;
		for (File file : thumbsDirectory.listFiles()) {
			if (file.isDirectory()) {
				thumbsSubDirectory = file;
				break;
			}
		}

		if (thumbsSubDirectory == null) {
			Log.d("DownloadInfo", "No thumbnail subfolder found for "
					+ infoHash);
			return null;
		}

		return findImage(thumbsSubDirectory);
	}

	private File findImage(File directory) {
		File[] foundImages = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {
				return name.endsWith(".png") || name.endsWith(".gif")
						|| name.endsWith(".jpg") || name.endsWith(".jpeg");
			}
		});

		// TODO: Find the best one
		if (foundImages.length > 0) {
			return foundImages[0];
		} else {
			Log.d("DownloadInfo", "No thumbnailimages found: "
					+ foundImages.length);
			return null;
		}
	}

	@Override
	public void onPoll() {
		XMLRPCDownloadManager.getInstance().getProgressInfo(
				mDownload.getInfoHash());
		Download currDownload = XMLRPCDownloadManager.getInstance()
				.getCurrentDownload();
		if (currDownload != null
				&& currDownload.getInfoHash().equals(mDownload.getInfoHash())) {
			mDownload = currDownload;
			fillLayout();
		}
	}
}