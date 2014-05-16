package com.rapidminer.lcm.obj;

public class SupportPatternObject {
	private Integer support;
	private String pattern;

	public SupportPatternObject(Integer support, String pattern) {
		this.support = support;
		this.pattern = pattern;
	}

	public Integer getSupport() {
		return support;
	}

	public void setSupport(Integer support) {
		this.support = support;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

}
