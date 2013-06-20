package ru.efive.wf.core.activity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ru.efive.wf.core.IActivity;
import ru.efive.wf.core.ProcessedData;

public final class RunnableActivityGroup implements IActivity {
	
	public RunnableActivityGroup() {
		
	}
	
	@Override
	public <T extends ProcessedData> boolean initialize(T t) {
		boolean result = false;
		try {
			parse();
			threadExecutor = Executors.newFixedThreadPool(activities.size());
			for (IActivity activity:activities) {
				activity.initialize(t);
			}
			result = true;
		}
		catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	private void parse() {
		
	}
	
	@Override
	public boolean execute() {
		for (IActivity activity:activities) {
			RunnableActivity ra = new RunnableActivity(activity);
			threadExecutor.execute(ra);
		}
		return true;
	}
	
	@Override
	public boolean dispose() {
		boolean result = false;
		threadExecutor.shutdown();
		try {
			threadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			result = true;
		} catch (InterruptedException e) {
		  result = false;
		  e.printStackTrace();
		}
		return result;
	}
	
	
	public String getResult() {
		return resultMessage;
	}
	
	private class RunnableActivity implements Runnable {
		
		public RunnableActivity(IActivity activity) {
			this.activity = activity;
		}
		
		@Override
		public void run() {
			activity.execute();
			activity.dispose();
		}
		
		private IActivity activity;
	}
	
	
	private List<IActivity> activities;
	
	private ExecutorService threadExecutor;
	
	private String resultMessage;
}