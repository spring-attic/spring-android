package org.grails.greenhouse.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

/**
 * @author Burt Beckwith
 */
public class ErrorReporter implements Thread.UncaughtExceptionHandler {

	private Thread.UncaughtExceptionHandler _previousHandler;
	private Context _currentContext;

	private static final int MAX_EMAIL = 5;

	public static ErrorReporter INSTANCE = new ErrorReporter();

	public void init(final Context context) {
		_currentContext = context;
		if (_previousHandler != null) {
			return;
		}
		_previousHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(
	 * 	java.lang.Thread, java.lang.Throwable)
	 */
	public void uncaughtException(final Thread thread, final Throwable t) {
		StringBuilder text = new StringBuilder();
		text.append("Error Report collected on : ").append(new Date());
		text.append("\n");
		text.append("\n");
		text.append("Information :");
		text.append("\n");
		text.append("==============");
		text.append("\n");
		text.append("\n");
		text.append(generateMessage());
		text.append("\n\n");
		text.append("Stack : \n");
		text.append("======= \n");

		appendException(text, t);

		text.append("****  End of current Report ***");
		saveAsFile(text.toString());
		// SendErrorMail( Report );
		_previousHandler.uncaughtException(thread, t);
	}

	private void sendErrorMail(final String message) {
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "burtbeckwith@gmail.com" });
		sendIntent.putExtra(Intent.EXTRA_TEXT, message);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Crash Report");
		sendIntent.setType("message/rfc822");
		_currentContext.startActivity(Intent.createChooser(sendIntent, "Title:"));
	}

	public void checkErrorAndSendMail() {
		try {
			String filePath = _currentContext.getFilesDir().getAbsolutePath();
			String[] errorFiles = getErrorFileList();
			if (errorFiles.length > 0) {
				StringBuilder errorText = new StringBuilder();
				int index = 0;
				for (String fileName : errorFiles) {
					File file = new File(filePath, fileName);
					if (index++ <= MAX_EMAIL) {
						errorText.append("New Trace collected :\n");
						errorText.append("=====================\n ");
						BufferedReader input = new BufferedReader(new FileReader(file));
						String line;
						while ((line = input.readLine()) != null) {
							errorText.append(line).append("\n");
						}
						input.close();
					}

					file.delete();
				}
				sendErrorMail(errorText.toString());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private long getAvailableInternalMemorySize() {
		StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
		return stat.getAvailableBlocks() * stat.getBlockSize();
	}

	private long getTotalInternalMemorySize() {
		StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
		return stat.getBlockCount() * stat.getBlockSize();
	}

	private void appendException(final StringBuilder text, final Throwable t) {
		Writer result = new StringWriter();
		PrintWriter printWriter = new PrintWriter(result);
		t.printStackTrace(printWriter);
		text.append(result);

		text.append("\n");
		text.append("Cause : \n");
		text.append("======= \n");

		Throwable cause = t.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			text.append(result);
			cause = cause.getCause();
		}
		printWriter.close();
	}

	private void saveAsFile(final String text) {
		try {
			int random = new Random().nextInt(99999);
			String FileName = "stack-" + random + ".stacktrace";
			FileOutputStream trace = _currentContext.openFileOutput(FileName, Context.MODE_PRIVATE);
			trace.write(text.getBytes());
			trace.close();
		}
		catch (Exception e) {
			// ...
		}
	}

	private String generateMessage() {
		try {
			PackageManager pm = _currentContext.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(_currentContext.getPackageName(), 0);

			return "Version : " + pi.versionName + "\n" +
			"Package : " + pi.packageName + "\n" +
			"FilePath : " + _currentContext.getFilesDir().getAbsolutePath() + "\n" +
			"Phone Model" + Build.MODEL + "\n" +
			"Android Version : " + Build.VERSION.RELEASE + "\n" +
			"Board : " + Build.BOARD + "\n" +
			"Brand : " + Build.BRAND + "\n" +
			"Device : " + Build.DEVICE + "\n" +
			"Display : " + Build.DISPLAY + "\n" +
			"FingerPrint : " + Build.FINGERPRINT + "\n" +
			"Host : " + Build.HOST + "\n" +
			"ID : " + Build.ID + "\n" +
			"Model : " + Build.MODEL + "\n" +
			"Product : " + Build.PRODUCT + "\n" +
			"Tags : " + Build.TAGS + "\n" +
			"Time : " + Build.TIME + "\n" +
			"Type : " + Build.TYPE + "\n" +
			"User : " + Build.USER + "\n" +
			"Total Internal memory : " + getTotalInternalMemorySize() + "\n" +
			"Available Internal memory : " + getAvailableInternalMemorySize() + "\n";
		}
		catch (Exception e) {
			e.printStackTrace();
			return "Error getting information: " + e.getMessage();
		}
	}

	private String[] getErrorFileList() {
		File dir = new File(_currentContext.getFilesDir().getAbsolutePath());
		dir.mkdir();
		return dir.list(new FilenameFilter() {
			public boolean accept(final File f, final String name) {
				return f.isFile() && name.endsWith(".stacktrace");
			}
		});
	}
}
