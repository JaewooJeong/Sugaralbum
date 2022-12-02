package com.kiwiple.multimedia.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.Version;
import com.kiwiple.multimedia.canvas.PixelCanvas;
import com.kiwiple.multimedia.canvas.Visualizer;
import com.kiwiple.multimedia.json.JsonObject;

/**
 * 라이브러리 개발 목적으로 사용하는 클래스입니다. 라이브러리 외부에서의 사용에 대해서는 그 유효성을 보장하지 않습니다.
 */
public final class DebugUtils {

	public static boolean isDebuggable(Context context) {
		Precondition.checkNotNull(context);

		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			ApplicationInfo applicationInfo = packageInfo.applicationInfo;
			return ((applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
		} catch (NameNotFoundException exception) {
			return Precondition.assureUnreachable();
		}
	}

	/**
	 * dumpJsonObject.
	 * 
	 * @param visualizer
	 * @param fileNamePrefix
	 *            null-ok;
	 */
	public static void dumpJsonObject(Visualizer visualizer, String fileNamePrefix) {
		Precondition.checkNotNull(visualizer);

		try {
			dumpJsonObject(visualizer.toJsonObject(), fileNamePrefix);
		} catch (JSONException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * dumpJsonObject.
	 * 
	 * @param jsonObject
	 * @param fileNamePrefix
	 *            null-ok;
	 */
	public static void dumpJsonObject(JsonObject jsonObject, String fileNamePrefix) {
		Precondition.checkNotNull(jsonObject);

		if (Version.current.isDev == false) {
			return;
		}

		File folder = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "myLogcat");
		if (!folder.isDirectory()) {
			folder.mkdir();
		}

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
		String fileName = "json_" + dateFormatter.format(new Date()) + ".txt";
		if (fileNamePrefix != null && !fileNamePrefix.isEmpty()) {
			fileName = fileNamePrefix + "_" + fileName;
		}

		File jsonLogFile = new File(folder.getAbsoluteFile(), fileName);

		try {
			if (jsonLogFile.exists()) {
				jsonLogFile.delete();
			}
			jsonLogFile.createNewFile();
			FileOutputStream outputStream = new FileOutputStream(jsonLogFile);
			OutputStreamWriter writer = new OutputStreamWriter(outputStream);
			writer.write(jsonObject.toString(3));
			writer.close();
			outputStream.close();
		} catch (IOException | JSONException exception) {
			exception.printStackTrace();
		}
	}

	public static String createLog(Size[] canvasRequirement) {
		return createLog(canvasRequirement, 0);
	}

	public static String createLog(Size[] canvasRequirement, int indentationSize) {
		Precondition.checkArray(canvasRequirement).checkNotContainsNull();
		Precondition.checkNotNegative(indentationSize);

		StringBuilder builder = new StringBuilder((32 + indentationSize) * canvasRequirement.length);
		String indentation = StringUtils.repeat('\t', indentationSize);

		for (int i = 0; i != canvasRequirement.length; ++i) {
			Size size = canvasRequirement[i];

			if (builder.length() > 0)
				builder.append('\n');
			builder.append(indentation).append(i + 1).append(". ").append(size.toString());
			builder.append(String.format(" (%.2fMB)", PixelCanvas.measureMegabytes(size.product())));
		}
		return builder.toString();
	}

	private DebugUtils() {
		// do not instantiate.
	}
}
