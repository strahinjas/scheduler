package com.etf.os2.project.scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.Pcb.ProcessState;

// Shortest Job First Scheduler - Non-Preemptive SJF

public class SJFScheduler extends Scheduler {
	private static final long PERIOD = 1000;

	protected double alfa;
	protected List<Queue<Pcb>> queue;

	private Timer timer;

	public SJFScheduler(double alfa) {
		super();
		this.alfa = alfa;
		queue = new ArrayList<>();

		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() { age(); }
		}, PERIOD, PERIOD);
	}

	@Override
	public synchronized Pcb get(int cpuId) {
		// O(log(numOfProcesses))
		try {
			if (cpuId < 0) throw new IllegalArgumentException("illegal processor ID");

			if (queue.isEmpty()) return null;

			Pcb pcb = null;
			if (!queue.get(cpuId).isEmpty()) {
				pcb = queue.get(cpuId).remove();
			} else {
				int index = (cpuId + 1) % queue.size();
				while (index != cpuId && queue.get(index).isEmpty())
					index = (index + 1) % queue.size();
				
				if (index == cpuId) return null;
				
				pcb = queue.get(index).remove();
				pcb.getPcbData().setAffinity(cpuId);
			}

			pcb.setTimeslice(0);
			((SJFPcbData) pcb.getPcbData()).setRemovalTime(Pcb.getCurrentTime());
			return pcb;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public synchronized void put(Pcb pcb) {
		// O(log(numOfProcesses))
		try {
			if (pcb == null) throw new IllegalArgumentException("argument to put() is null");

			int index = minimalLoad();
			if (pcb.getPreviousState() == ProcessState.CREATED) {
				SJFPcbData data = new SJFPcbData(pcb.getPriority(), Pcb.getCurrentTime());
				data.setAffinity(index);
				pcb.setPcbData(data);
			} else {
				SJFPcbData data = (SJFPcbData) pcb.getPcbData();
				long burstTime = pcb.getExecutionTime();
				double prediction = data.getPrediction();
				data.setPrediction(alfa * burstTime + (1 - alfa) * prediction);
				data.setScheduleTime(Pcb.getCurrentTime());
				
				index = data.getAffinity();
			}

			queue.get(index).add(pcb);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	private int minimalLoad() {
		// O(numOfCPUs)
		if (queue.isEmpty()) {
			for (int i = 0; i < Pcb.RUNNING.length; i++)
				queue.add(new PriorityQueue<>(Comparator.comparing(Pcb::getPcbData)));

			return 0;
		}

		int index = 0;
		int min = queue.get(0).size();
		for (int i = 1; i < queue.size(); i++) {
			if (min == 0) break;
			int load = queue.get(i).size();
			if (min > load) {
				min = load;
				index = i;
			}
		}

		return index;
	}

	private synchronized void age() {
		// O(numOfProcesses)
		if (queue.isEmpty()) return;

		for (Queue<Pcb> subqueue : queue) {
			for (Pcb pcb : subqueue) {
				SJFPcbData data = (SJFPcbData) pcb.getPcbData();
				long waitingTime = Pcb.getCurrentTime() - data.getScheduleTime();
				double burstTime = data.getPrediction();
				data.setPrediction(burstTime / waitingTime + burstTime);
			}

			subqueue.add(subqueue.remove());
		}
	}

}
