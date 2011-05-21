package org.zkoss.less.util;

import java.io.File;
import java.io.IOException;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;

public class LessZUtil {
	private static LessEngine engine = new LessEngine();

	public static void compileLessFile(String filePath) throws LessException,
			IOException {
		engine.compile(new File(filePath), new File(filePath + ".css"));
	}

	public static String compileLessContent(String content)
			throws LessException {
		return engine.compile(content);
	}
}
