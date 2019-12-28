package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;

public abstract class Scheduler
{

	public abstract Pcb get(int cpuId);

	public abstract void put(Pcb pcb);

	public static Scheduler createScheduler(String[] args)
	{
		try
		{
			switch (args[0].toUpperCase())
			{
				case "SJF":
					double alfa = Double.parseDouble(args[2]);
					if (alfa < 0 || alfa > 1)
						throw new IllegalArgumentException("illegal smoothing factor");

					if (args[1].toUpperCase().equals("P"))
						return new SRTFScheduler(alfa);
					else if (args[1].toUpperCase().equals("NP"))
						return new SJFScheduler(alfa);
					else
						throw new IllegalArgumentException("nonexistent sjf type");
				case "MFQ":
					int levels = Integer.parseInt(args[1]);
					if (levels < 0 || args.length < levels + 2)
						throw new IllegalArgumentException("missing quanta");

					long[] quantum = new long[levels];
					for (int i = 0; i < levels; i++)
						quantum[i] = Long.parseLong(args[i + 2]);
					return new MFQScheduler(levels, quantum);
				case "CF":
					return new CFScheduler();
				default:
					throw new IllegalArgumentException("nonexistent scheduler type");
			}
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
