package org.zkoss.less;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.zkoss.lang.Objects;
import org.zkoss.less.util.LessZUtil;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.sys.HtmlPageRenders;
import org.zkoss.zul.Style;
import org.zkoss.zul.impl.Utils;

import com.asual.lesscss.LessException;

public class LessStyle extends Style implements org.zkoss.less.api.LessStyle {
	public static final String LESSRESOURCE = "org.zkoss.less.LessResource";
	public static final String LESSMODEINSTANT = "instant";
	public static final String LESSMODESTATIC = "static";

	private String _src;
	private String _content;
	private String _media;
	private byte _cntver;
	private boolean _recompile = true;
	private String _mode = LESSMODEINSTANT;
	private String _serviceURI = "/less/";

	public LessStyle() {
	}

	public boolean isRecompile() {
		return _recompile;
	}

	public void setRecompile(boolean reCompile) {
		this._recompile = reCompile;
	}

	public String getMode() {
		return _mode;
	}

	public void setMode(String mode) {
		this._mode = mode;
	}

	public String getServiceURI() {
		return this._serviceURI;
	};

	public void setServiceURI(String uri) {
		this._serviceURI = uri;
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
			if (_mode.equals(LESSMODEINSTANT)) {
				try {
					String path = Executions.getCurrent().getDesktop().getWebApp().getRealPath(src);
					if (!new File(path + ".css").exists() || _recompile)
						LessZUtil.compileLessFileToCSSFile(path);
				} catch (LessException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				_src = src.replace(".less", ".css");
			} else if (_mode.equals(LESSMODESTATIC)) {
				_src = getServiceURI() + src.replace(".less", "");
			} else {
				_src = getServiceURI() + src;
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
				_content = LessZUtil.compileLessToString(content);
			} catch (LessException e) {
				e.printStackTrace();
			}
			_src = null;
			++_cntver;
			smartUpdate("src", new EncodedURL());
		}
	}

	protected void renderProperties(org.zkoss.zk.ui.sys.ContentRenderer renderer) throws java.io.IOException {
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
