package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.PcbData;

public class CFPcbData extends PcbData
{
	private long runTime, scheduleTime;

	public CFPcbData(int priority, long insertionTime)
	{
		super(priority);
		this.scheduleTime = insertionTime;
	}

	public long getRunTime()
	{
		return runTime;
	}

	public void setRunTime(long runTime)
	{
		this.runTime = runTime;
	}

	public long getScheduleTime()
	{
		return scheduleTime;
	}

	public void setScheduleTime(long scheduleTime)
	{
		this.scheduleTime = scheduleTime;
	}

	@Override
	public int compareTo(PcbData o)
	{
		int result = Long.compare(runTime, ((CFPcbData) o).runTime);
		return result == 0 ? super.compareTo(o) : result;
	}

}
