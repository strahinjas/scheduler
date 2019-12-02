package com.etf.os2.project.scheduler;

import java.util.HashSet;
import java.util.Set;

import com.etf.os2.project.process.Pcb;

// Shortest Remaining Time First Scheduler - Preemptive SJF

public class SRTFScheduler extends SJFScheduler {

	public SRTFScheduler(double alfa) { super(alfa); }

	@Override
	public synchronized void put(Pcb pcb) {
		// O(log(numOfProcesses))
		super.put(pcb);

		SJFPcbData data = (SJFPcbData) pcb.getPcbData();
		int affinity = data.getAffinity();
		double prediction = data.getPrediction();
		
		int maxCPU = 0;
		double maxRemainingTime = 0;
		Set<Integer> set = new HashSet<>();
		for (int i = 0; i < Pcb.RUNNING.length; i++) {
			data = (SJFPcbData) Pcb.RUNNING[i].getPcbData();
			if (data == null) continue;
			
			double remainingTime = data.getPrediction() - (Pcb.getCurrentTime() - data.getRemovalTime());
			
			if (maxRemainingTime < remainingTime) {
				maxCPU = i;
				maxRemainingTime = remainingTime;
			}
			
			if (prediction < remainingTime) set.add(i);
		}
		if (!set.isEmpty()) {
			if (set.contains(affinity))
				Pcb.RUNNING[affinity].preempt();
			else
				Pcb.RUNNING[maxCPU].preempt();
		}
	}

}
