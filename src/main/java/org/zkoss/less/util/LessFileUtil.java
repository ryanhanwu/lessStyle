package org.zkoss.less.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

public class LessFileUtil {
	public static String readFileToString(File file) throws IOException {
		StringBuffer fileData = new StringBuffer(2048);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}
	public static String readFilePathToString(String filePath) throws IOException {
		StringBuffer fileData = new StringBuffer(2048);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

	public static byte[] readFileToBinary(File lessCSS) throws IOException {
		byte[] result;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		FileInputStream input = new FileInputStream(lessCSS);
		try {
			byte[] buffer = new byte[1024];
			int bytesRead = -1;
			while ((bytesRead = input.read(buffer)) != -1) {
				byteStream.write(buffer, 0, bytesRead);
			}
			result = byteStream.toByteArray();
		} finally {
			byteStream.close();
			input.close();
		}
		return result;
	}
}
