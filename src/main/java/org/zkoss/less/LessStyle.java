package org.zkoss.less;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.zkoss.lang.Library;
import org.zkoss.lang.Objects;
import org.zkoss.less.util.LessZUtil;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.sys.HtmlPageRenders;
import org.zkoss.zul.Style;
import org.zkoss.zul.impl.Utils;

import com.asual.lesscss.LessException;

public class LessStyle extends Style implements org.zkoss.less.api.LessStyle {
	public static final String LESS_RESOURCE = "org.zkoss.less.LessResource";
	public static final String LESS_DEBUG = "org.zkoss.less.LessDebug";
	private String _src;
	/** _src and _content cannot be nonnull at the same time. */
	private String _content;
	private String _media;
	/** Count the version of {@link #_content}. */
	private byte _cntver;
	private boolean recompile = true;

	public boolean isRecompile() {
		return recompile;
	}

	public void setRecompile(boolean reCompile) {
		this.recompile = reCompile;
	}

	public LessStyle() {
	}

	public String getSrc() {
		return _src;
	}

	/**
	 * Set Less file source location
	 * */
	public void setSrc(String src) {
		if (src != null && src.length() == 0)
			src = null;
		if (!src.endsWith(".less"))
			throw new UnsupportedOperationException("LessStyle can only compile file end with .less");

		if (_content != null || !Objects.equals(_src, src)) {
			String debug = Library.getProperty(LESS_DEBUG);
			if (Boolean.valueOf(debug)) {
				try {
					String path = Executions.getCurrent().getDesktop().getWebApp().getRealPath(src);
					if (!new File(path + ".css").exists() || recompile)
						LessZUtil.compileLessFile(path);
					;
				} catch (LessException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				_src = src + ".css";
			} else {
				// Get Resource From Less Service
				_src = "/less/" + src;
			}
			_content = null;
			smartUpdate("src", new EncodedURL());
		}
	}

	public String getMedia() {
		return _media;
	}

	public void setMedia(String media) {
		if (media != null && media.length() == 0)
			media = null;
		if (!Objects.equals(_media, media)) {
			_media = media;
			smartUpdate("media", _media);
		}
	}

	public String getContent() {
		return _content;
	}

	public void setContent(String content) {
		if (content != null && content.length() == 0)
			content = null;

		if (_src != null || !Objects.equals(_content, content)) {
			try {
				_content = LessZUtil.compileLessContent(content);
			} catch (LessException e) {
				e.printStackTrace();
			}
			_src = null;
			++_cntver;
			smartUpdate("src", new EncodedURL());
			// AU: always uses src to solve IE/Chrome/... issue
		}
	}

	protected void renderProperties(org.zkoss.zk.ui.sys.ContentRenderer renderer) throws java.io.IOException {
		super.renderProperties(renderer);

		boolean gened = false;
		final String cnt = getContent();
		// allow derive to override getContent()
		if (cnt != null) {
			final HtmlPageRenders.RenderContext rc = HtmlPageRenders.getRenderContext(null);
			if (rc != null && rc.perm != null) {
				final Writer out = rc.perm;
				// don't use rc.temp which will be replaced with widgets later
				out.write("\n<style id=\"");
				out.write(getUuid());
				out.write("-css\" type=\"text/css\"");
				if (_media != null) {
					out.write(" media=\"");
					out.write(_media);
					out.write('"');
				}
				out.write(">\n");
				out.write(cnt);
				out.write("\n</style>\n");
				gened = true;
			}
		}
		if (!gened) {
			render(renderer, "src", getEncodedURL());
			render(renderer, "media", _media);
		}
	}

	/**
	 * Returns the encoded URL of the image (never null).
	 */
	private String getEncodedURL() {
		if (getContent() != null) // allow derived to override getContent()
			return Utils.getDynamicMediaURI(this, _cntver, "css", "css");

		if (_src != null) {
			final Desktop dt = getDesktop();
			if (dt != null)
				return dt.getExecution().encodeURL(_src);

		}
		return "";
	}

	private class EncodedURL implements org.zkoss.zk.ui.util.DeferredValue {
		public Object getValue() {
			return getEncodedURL();
		}
	}
}
