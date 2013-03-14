package com.zmansiv.plugin.chat.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlMessageRow {

	private final String source;
	private final String message;
	private final long time;
	private final Type type;

	public HtmlMessageRow(com.mercuryirc.model.Message message, Type type) {
		this.type = type;
		this.time = message.getTimestamp();
		String _message = escapeLine(stripColors(message.getMessage()));
		switch (type) {
			case PRIVMSG:
				this.source = message.getSource().getName();
				this.message = _message;
				break;
			case NOTICE:
				this.source = message.getSource().getName();
				this.message = message.getTarget() == null ? _message : "to " + message.getTarget().getName() + ": " + _message;
				break;
			case CTCP:
				this.source = "";
				this.message = _message;
				break;
			default:
				this.source = "";
				this.message = (message.getSource() == null ? "" : (message.getSource().getName() + " ")) + _message;
		}
	}

	private static String stripColors(String line) {
		return line.replaceAll("[\u0003]+[\\d](\\d)?(,[\\d](\\d)?)?", "");
	}

	private static String escapeLine(String line) {
		StringBuilder result = new StringBuilder();
		Matcher m = Pattern.compile("(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))").matcher(line);
		int lastMatchEnd = 0;
		while (m.find()) {
			String url = m.group();
			if (m.start() != 0) {
				result.append(escape(line.substring(lastMatchEnd, m.start())));
			}
			lastMatchEnd = m.end();
			String escaped = escape(url);
			result.append(String.format("<a href=javascript:onLinkClick(\\'%s\\')>%s</a>", escaped, escaped));
		}
		if (lastMatchEnd != line.length()) {
			result.append(escape(line.substring(lastMatchEnd)));
		}
		return result.toString();
	}

	private static String escape(String line) {
		return line.replace("\\", "\\\\").replace("&", "&amp;").replace("'", "&apos;").replace("\"", "&quot;").replace("<", "&#60;").replace(">", "&gt;");
	}

	public String source() {
		return source;
	}

	public String message() {
		return message;
	}

	public long time() {
		return time;
	}

	public Type type() {
		return type;
	}

	public enum Type {

		ERROR("error", true),
		NOTICE("notice", true),
		CTCP("ctcp", true),
		PRIVMSG("privmsg", true),
		EVENT("event", false),
		JOIN("join", false),
		PART("part", false);

		private final String style;
		private final boolean alert;

		private Type(String style, boolean notify) {
			this.style = style;
			this.alert = notify;
		}

		public String style() {
			return style;
		}

		public boolean alert() {
			return alert;
		}

	}

}