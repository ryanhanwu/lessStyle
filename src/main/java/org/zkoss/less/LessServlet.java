package org.zkoss.less;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.less.util.LessZUtil;

import com.asual.lesscss.LessException;
import com.yahoo.platform.yui.compressor.CssCompressor;

public class LessServlet extends HttpServlet {
	protected int maxAge = 31556926;
	protected long milliseconds = 1000L;

	private static final String INSTANT = "org.zkoss.less.Instant";
	private static boolean mode_instant = false;
	private static ServletContext sc;
	private static String lessResource;

	@Override
	public void init(ServletConfig config) throws ServletException {
		lessResource = config.getInitParameter(LessStyle.LESS_RESOURCE);
		mode_instant = config.getInitParameter(INSTANT) != null;
		sc = config.getServletContext();
		try {
			if (!mode_instant)
				initLess(lessResource);
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		try {
			String requestFileName = sc.getRealPath(lessResource + request.getPathInfo().replaceAll("/+", "/"));
			File lessCSSFile = new File(requestFileName + ".css");
			byte[] content = null;

			response.setContentType("text/css;charset=UTF-8");
			response.setDateHeader("Expires", System.currentTimeMillis() + maxAge * milliseconds);
			response.setHeader("Cache-control", "max-age=" + maxAge);

			if (lessCSSFile.exists() && !mode_instant) {// Default Mode
				content = readFileToBinary(lessCSSFile);
				long ifModifiedSince = request.getDateHeader("If-Modified-Since");
				if (ifModifiedSince != 0 && ifModifiedSince / milliseconds == lessCSSFile.lastModified() / milliseconds) {
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}
				response.setDateHeader("Last-Modified", lessCSSFile.lastModified());
			} else {// Instant Mode
				content = LessZUtil.compressCSSToString(
						LessZUtil.compileLessToString(readFileToString(requestFileName + ".less"))).getBytes();
			}
			response.setContentLength(content.length);
			response.getOutputStream().write(content);
			response.getOutputStream().flush();
			response.getOutputStream().close();

		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
	}

	/** 
	 * Compile .less to .css
	 * */
	private void initLess(String lessSrc) throws IOException, LessException {
		for (Iterator<String> it = sc.getResourcePaths(lessSrc).iterator(); it.hasNext();) {
			String filename = it.next();
			String realPath = sc.getRealPath(filename);
			if (filename.endsWith(".less")) {
				String newFilePath = LessZUtil.compileLessFileToCSSFile(realPath);
				Reader in = new FileReader(newFilePath);
				CssCompressor compressor = new CssCompressor(in);
				in.close();
				FileWriter out = new FileWriter(new File(newFilePath));
				compressor.compress(out, -1);
				out.flush();

			}
		}
	}

	private static String readFileToString(String filePath) throws IOException {
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

	private static byte[] readFileToBinary(File lessCSS) throws IOException {
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
