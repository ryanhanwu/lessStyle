package org.zkoss.less.api;

public interface LessStyle extends org.zkoss.zul.api.Style {

	public String getServiceURI();

	public void setServiceURI(String uri);

	public boolean isRecompile();

	public void setRecompile(boolean reCompile);

	public String getMode();

	public void setMode(String mode);

}
