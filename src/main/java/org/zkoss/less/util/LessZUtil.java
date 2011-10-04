package org.zkoss.less.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;
import com.yahoo.platform.yui.compressor.CssCompressor;

public class LessZUtil {
	private static LessEngine engine = new LessEngine();

	public static String compileLessFileToCSSFile(String filePath) throws LessException, IOException {
		String newPath = filePath.replace(".less", ".css");
		engine.compile(new File(filePath), new File(newPath));
		return newPath;
	}

	public static String compileLessToString(String lessContent) throws LessException {
		return engine.compile(lessContent);
	}

	public static String compressCSSToString(String cssContent) throws IOException {
		StringReader sr = new StringReader(cssContent);
		CssCompressor compressor = new CssCompressor(sr);
		sr.close();
		StringWriter sw = new StringWriter();
		compressor.compress(sw, -1);
		return sw.toString();
	}
}
