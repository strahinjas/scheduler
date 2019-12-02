package com.etf.os2.project.process;

public class PcbData implements Comparable<PcbData> {
	protected int priority;
	protected int affinity;

	public PcbData(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getAffinity() {
		return affinity;
	}

	public void setAffinity(int affinity) {
		this.affinity = affinity;
	}

	@Override
	public int compareTo(PcbData o) {
		return Integer.compare(priority, o.priority);
	}

}
