package org.zkoss.less.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.less.LessStyle;
import org.zkoss.less.util.LessZUtil;

import com.asual.lesscss.LessException;
import com.yahoo.platform.yui.compressor.CssCompressor;

public class LessServlet extends HttpServlet {
	protected int maxAge = 31556926;
	protected long milliseconds = 1000L;
	private static String lessPath;
	private static ServletContext sc;
	private static String lessSrc;

	@Override
	public void init(ServletConfig config) throws ServletException {
		lessSrc = config.getInitParameter(LessStyle.LESS_RESOURCE);
		sc = config.getServletContext();
		lessPath = sc.getRealPath(lessSrc);
		try {
			initializeLess(lessSrc);
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		try {
			File lessCSS = new File(lessPath + request.getRequestURI().replaceAll(lessSrc, "").replaceAll("/+", "/")
					+ ".css");
			byte[] content = readBinaryFile(lessCSS);

			long ifModifiedSince = request.getDateHeader("If-Modified-Since");
			if (ifModifiedSince != 0 && ifModifiedSince / milliseconds == lessCSS.lastModified() / milliseconds) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}

			response.setContentType("text/css;charset=UTF-8");
			response.setDateHeader("Expires", System.currentTimeMillis() + maxAge * milliseconds);
			response.setHeader("Cache-control", "max-age=" + maxAge);
			response.setDateHeader("Last-Modified", lessCSS.lastModified());
			response.setContentLength(content.length);
			response.getOutputStream().write(content);
			response.getOutputStream().flush();
			response.getOutputStream().close();

		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private void initializeLess(String lessSrc) throws IOException, LessException {
		for (Iterator<String> it = sc.getResourcePaths(lessSrc).iterator(); it.hasNext();) {
			String filename = it.next();
			String realPath = sc.getRealPath(filename);
			if (filename.endsWith(".less")) {
				System.out.println("File " + realPath + " compressed to " + realPath + ".css");
				FileWriter out = new FileWriter(new File(realPath + ".css"));
				LessZUtil.compileLessFile(realPath);
				Reader in = new FileReader(realPath + ".css");
				CssCompressor compressor = new CssCompressor(in);
				in.close();
				compressor.compress(out, -1);
				out.flush();
			}
		}
	}

	public static byte[] readBinaryFile(File lessCSS) throws IOException {
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
