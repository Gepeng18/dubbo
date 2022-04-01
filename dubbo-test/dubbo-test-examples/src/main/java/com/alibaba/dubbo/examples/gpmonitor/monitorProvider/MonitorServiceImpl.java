package com.alibaba.dubbo.examples.gpmonitor.monitorProvider;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.monitor.MonitorService;

import java.util.List;

@Service
@org.springframework.stereotype.Service
public class MonitorServiceImpl implements MonitorService {
	@Override
	public void collect(URL statistics) {
		if (statistics!=null){
			String application = statistics.getParameter(MonitorService.APPLICATION);
			String service = statistics.getParameter(MonitorService.INTERFACE);
			String method = statistics.getParameter(MonitorService.METHOD);
			String group = statistics.getParameter(MonitorService.GROUP);
			String version = statistics.getParameter(MonitorService.VERSION);
			String client = statistics.getParameter(MonitorService.CONSUMER);
			String server = statistics.getParameter(MonitorService.PROVIDER);
			String timestamp = statistics.getParameter(MonitorService.TIMESTAMP);
			String success = statistics.getParameter(MonitorService.SUCCESS);
			String failure = statistics.getParameter(MonitorService.FAILURE);
			String input = statistics.getParameter(MonitorService.INPUT);
			String output= statistics.getParameter( MonitorService.OUTPUT);
			String elapsed = statistics.getParameter(MonitorService.ELAPSED);
			String concurrent = statistics.getParameter(MonitorService.CONCURRENT);
			String maxInput = statistics.getParameter(MonitorService.MAX_INPUT);
			String maxOutput = statistics.getParameter(MonitorService.MAX_OUTPUT);
			String maxElapsed = statistics.getParameter(MonitorService.MAX_ELAPSED);
			String maxConcurrent = statistics.getParameter(MonitorService.MAX_CONCURRENT);
			System.out.println("application:"+application
					+",service:"+service+",method:"+method+",group:"+group+",version:"+version+",client:"+client+",server:"+server
					+",timestamp:"+timestamp+",success:"+success+",failure:"+failure+",input:"+input+",output:"+output
					+",elapsed:"+elapsed+",concurrent:"+concurrent+",maxInput:"+maxInput+",maxOutput:"+maxOutput+",maxElapsed:"+maxElapsed
					+",maxConcurrent:"+maxConcurrent
			);


		}

	}

	@Override
	public List<URL> lookup(URL query) {
		return null;
	}
}
