package com.etf.os2.project.scheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.Pcb.ProcessState;
import com.etf.os2.project.process.PcbData;

// Multilevel Feedback Queue Scheduler

public class MFQScheduler extends Scheduler {
	private static final long PERIOD = 1000;
	
	private long[] quantum;
	private int[][] affinities;
	private List<List<Pcb>> queue;

	private Timer timer;

	public MFQScheduler(int levels, long[] quantum) {
		super();
		this.quantum = quantum;
		queue = new ArrayList<>(levels);
		for (int i = 0; i < levels; i++)
			queue.add(new LinkedList<>());

		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() { age(); }
		}, PERIOD, PERIOD);
	}

	@Override
	public synchronized Pcb get(int cpuId) {
		// O(numOfProcesses)
		try {
			if (cpuId < 0) throw new IllegalArgumentException("illegal processor ID");

			Pcb pcb = null;
			for (int i = 0; i < queue.size(); i++) {
				List<Pcb> level = queue.get(i);
				if (level.isEmpty()) continue;
				
				if (affinities[cpuId][i] > 0) {
					for (Pcb process : level)
						if (process.getAffinity() == cpuId) {
							pcb = process; break;
						}
					level.remove(pcb);
					--affinities[cpuId][i];
				} else {
					pcb = level.remove(0);
					pcb.getPcbData().setAffinity(cpuId);
				}
				break;
			}
			
			if (pcb == null) return null;

			pcb.setTimeslice(quantum[pcb.getPcbData().getPriority()]);
			return pcb;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public synchronized void put(Pcb pcb) {
		// O(1)
		try {
			if (pcb == null) throw new IllegalArgumentException("argument to put() is null");
			
			if (affinities == null)
				affinities = new int[Pcb.RUNNING.length][queue.size()];

			int priority = pcb.getPriority() % queue.size();
			ProcessState state = pcb.getPreviousState();
			if (state == ProcessState.CREATED) {
				PcbData data = new PcbData(priority);
				pcb.setPcbData(data);
			} else {
				PcbData data = pcb.getPcbData();
				priority = data.getPriority();
				if (state == ProcessState.BLOCKED) {
					if (priority > 0) --priority;
				} else {
					if (priority < queue.size() - 1) ++priority;
				}
				data.setPriority(priority);
			}

			++affinities[pcb.getAffinity()][priority];
			queue.get(priority).add(pcb);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	private synchronized void age() {
		// O(numOfProcesses)
		for (int i = 1; i < queue.size(); i++) {
			if (queue.get(i).isEmpty()) continue;
			
			queue.get(i).forEach(pcb ->
			pcb.getPcbData().setPriority(pcb.getPcbData().getPriority() - 1));
			
			queue.get(i - 1).addAll(queue.get(i));
			queue.get(i).clear();
		}
		affinities = new int[Pcb.RUNNING.length][queue.size()];
	}

}
