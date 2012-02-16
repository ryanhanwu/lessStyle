package org.zkoss.less;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.less.util.LessFileUtil;
import org.zkoss.less.util.LessZUtil;

import com.asual.lesscss.LessException;
import com.yahoo.platform.yui.compressor.CssCompressor;

public class LessServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	protected int maxAge = 31556926;
	protected long milliseconds = 1000L;

	private static final String INSTANT = "org.zkoss.less.Instant";
	private static final String EXT_LESS = ".less";
	private static boolean mode_instant = false;
	private static ServletContext sc;
	private static String lessResource;
	private static Map<String, Long> fileModified = new HashMap<String, Long>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		lessResource = config.getInitParameter(LessStyle.LESSRESOURCE);
		mode_instant = config.getInitParameter(INSTANT) != null;
		sc = config.getServletContext();
		try {
			if (!mode_instant)
				initLessResource(lessResource);
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		try {
			String requestFileName = sc.getRealPath(lessResource + request.getPathInfo().replaceAll("/+", "/"));
			File lessSource = new File(requestFileName + EXT_LESS);
			File lessOutputCSS = new File(requestFileName + ".css");
			byte[] content = null;

			response.setContentType("text/css;charset=UTF-8");
			response.setDateHeader("Expires", System.currentTimeMillis() + maxAge * milliseconds);
			response.setHeader("Cache-control", "max-age=" + maxAge);

			if (mode_instant) {
				content = LessZUtil.compressCSSToString(
						LessZUtil.compileLessToString(LessFileUtil.readFileToString(lessSource))).getBytes();
			} else {
				Long lessFileLastModified = fileModified.get(requestFileName + EXT_LESS);
				// Less file updated
				if (lessFileLastModified < lessSource.lastModified()) {
					fileModified.put(requestFileName + EXT_LESS, lessSource.lastModified());
					compileFromLessResourcePathToCSS(requestFileName + EXT_LESS);
				}
				content = LessFileUtil.readFileToBinary(lessOutputCSS);
				long ifModifiedSince = request.getDateHeader("If-Modified-Since");
				if (ifModifiedSince != 0
						&& ifModifiedSince / milliseconds == lessOutputCSS.lastModified() / milliseconds) {
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}
				response.setDateHeader("Last-Modified", lessOutputCSS.lastModified());

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
	private void initLessResource(String lessSrc) throws IOException, LessException {
		for (Iterator<String> it = sc.getResourcePaths(lessSrc).iterator(); it.hasNext();) {
			String filePath = it.next();
			if (filePath.endsWith(EXT_LESS)) {
				compileFromLessResourcePathToCSS(sc.getRealPath(filePath));
			}
		}
	}

	private void compileFromLessResourcePathToCSS(String lessResourcePath) throws LessException, IOException {
		File lessFile = new File(lessResourcePath);
		fileModified.put(lessResourcePath, lessFile.lastModified());
		String newFilePath = LessZUtil.compileLessFileToCSSFile(lessFile);
		Reader in = new FileReader(newFilePath);
		CssCompressor compressor = new CssCompressor(in);
		in.close();
		FileWriter out = new FileWriter(new File(newFilePath));
		compressor.compress(out, -1);
		out.flush();
	}

}
