package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.PcbData;

public class SJFPcbData extends PcbData
{
	private long scheduleTime, removalTime;
	private double prediction = 10;

	public SJFPcbData(int priority, long insertionTime)
	{
		super(priority);
		this.scheduleTime = insertionTime;
	}

	public long getScheduleTime()
	{
		return scheduleTime;
	}

	public void setScheduleTime(long scheduleTime)
	{
		this.scheduleTime = scheduleTime;
	}

	public long getRemovalTime()
	{
		return removalTime;
	}

	public void setRemovalTime(long removalTime)
	{
		this.removalTime = removalTime;
	}

	public double getPrediction()
	{
		return prediction;
	}

	public void setPrediction(double prediction)
	{
		this.prediction = prediction;
	}

	@Override
	public int compareTo(PcbData o)
	{
		int result = Double.compare(prediction, ((SJFPcbData) o).prediction);
		return result == 0 ? super.compareTo(o) : result;
	}

}
