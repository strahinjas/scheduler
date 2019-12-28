package com.etf.os2.project.scheduler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.Pcb.ProcessState;

// Completely Fair Scheduler

public class CFScheduler extends Scheduler
{
	private static final int SCHEDULE_WAIT_SCALE = 128;
	private static final long DEFAULT_TIMESLICE = 10;

	private LoadBalance load;
	private List<Queue<Pcb>> queue;

	private class LoadBalance extends JFrame
	{
		private LoadBar[] loads;
		private LoadLabel[] labels;

		private class LoadBar extends JPanel
		{
			private int cpuId;
			private Color color;

			public LoadBar(int cpuId)
			{
				super();
				this.cpuId = cpuId;
				color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
			}

			@Override
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				setBackground(Color.WHITE);

				g.setColor(color);
				g.fillRect(0, 0, queue.get(cpuId).size() * getWidth() / Pcb.getProcessCount(), getHeight());
			}

		}

		private class LoadLabel extends JLabel
		{
			private int cpuId;

			public LoadLabel(int cpuId)
			{
				super();
				this.cpuId = cpuId;
				setText(cpuId + ": " + queue.get(cpuId).size());
				setHorizontalAlignment(SwingConstants.CENTER);
			}

			@Override
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				setText(cpuId + ": " + queue.get(cpuId).size());
				setHorizontalAlignment(SwingConstants.CENTER);
			}

		}

		public LoadBalance()
		{
			super("Load Balance");
			int numOfCPUs = Pcb.RUNNING.length;

			loads = new LoadBar[numOfCPUs];
			labels = new LoadLabel[numOfCPUs];
			JPanel west = new JPanel(new GridLayout(numOfCPUs, 1));
			JPanel center = new JPanel(new GridLayout(numOfCPUs, 1));
			for (int i = 0; i < numOfCPUs; i++)
			{
				loads[i] = new LoadBar(i);
				center.add(loads[i]);
				labels[i] = new LoadLabel(i);
				west.add(labels[i]);
			}
			add(west, BorderLayout.WEST);
			add(center, BorderLayout.CENTER);

			setVisible(true);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setBounds(600, 200, 400, 300);
		}

	}

	public CFScheduler()
	{
		super();
		queue = new ArrayList<>();
	}

	@Override
	public Pcb get(int cpuId)
	{
		// O(log(numOfProcesses))
		try
		{
			if (cpuId < 0) throw new IllegalArgumentException("illegal processor ID");

			if (queue.isEmpty()) return null;

			Pcb pcb;
			if (!queue.get(cpuId).isEmpty())
			{
				pcb = queue.get(cpuId).remove();
			}
			else
			{
				int index = (cpuId + 1) % queue.size();
				while (index != cpuId && queue.get(index).isEmpty())
					index = (index + 1) % queue.size();

				if (index == cpuId) return null;

				pcb = queue.get(index).remove();
				pcb.getPcbData().setAffinity(cpuId);
			}

			long waitingTime = Pcb.getCurrentTime() - ((CFPcbData) pcb.getPcbData()).getScheduleTime();
			long timeslice = waitingTime * SCHEDULE_WAIT_SCALE / Pcb.getProcessCount();
			if (timeslice == 0) timeslice = DEFAULT_TIMESLICE;

			pcb.setTimeslice(timeslice);
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					load.repaint();
				}
			});
			return pcb;
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void put(Pcb pcb)
	{
		// O(log(numOfProcesses))
		try
		{
			if (pcb == null) throw new IllegalArgumentException("argument to put() is null");

			int index = minimalLoad();
			if (pcb.getPreviousState() == ProcessState.CREATED)
			{
				CFPcbData data = new CFPcbData(pcb.getPriority(), Pcb.getCurrentTime());
				data.setAffinity(index);
				pcb.setPcbData(data);
			}
			else
			{
				CFPcbData data = (CFPcbData) pcb.getPcbData();
				data.setScheduleTime(Pcb.getCurrentTime());
				data.setRunTime(data.getRunTime() + pcb.getExecutionTime());

				index = data.getAffinity();
			}

			queue.get(index).add(pcb);
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					load.repaint();
				}
			});
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
	}

	private int minimalLoad()
	{
		// O(numOfCPUs)
		if (queue.isEmpty())
		{
			for (int i = 0; i < Pcb.RUNNING.length; i++)
				queue.add(new PriorityQueue<>(Comparator.comparing(Pcb::getPcbData)));

			load = new LoadBalance();
			return 0;
		}

		int index = 0;
		int min = queue.get(0).size();
		for (int i = 1; i < queue.size(); i++)
		{
			if (min == 0) break;
			int load = queue.get(i).size();
			if (min > load)
			{
				min = load;
				index = i;
			}
		}

		return index;
	}

}
