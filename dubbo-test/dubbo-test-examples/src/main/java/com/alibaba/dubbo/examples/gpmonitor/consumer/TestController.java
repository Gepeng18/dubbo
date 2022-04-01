package com.alibaba.dubbo.examples.gpmonitor.consumer;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class TestController {

	@Reference(check = false)
	private IHelloProviderService iHelloProviderService;

	ExecutorService threadPool = Executors.newFixedThreadPool(10);

	@RequestMapping("/tests")
	public String test(){
		for (int i=0;i<1000;++i) {
			threadPool.submit(new Runnable() {
								  @Override
								  public void run() {
									  iHelloProviderService.getName(1);
								  }
							  }
			);
		}
		return null;
	}
}

/**
 * xml配置
 <dubbo:application name="dubbo-consumer">
 <dubbo:parameter key="qos.enable" value="true"/>
 <dubbo:parameter key="qos.accept.foreign.ip" value="false"/>
 <dubbo:parameter key="qos.port" value="8108"/>
 </dubbo:application>
 <dubbo:monitor  address="dubbo://127.0.0.1:18109"></dubbo:monitor>
 <dubbo:registry protocol="zookeeper" address="127.0.0.1:2181" client="curator" >
 <dubbo:parameter key="save.file" value="true"></dubbo:parameter>
 </dubbo:registry>
 <dubbo:protocol accesslog="true" name="dubbo" port="18108" />

 */


/**
 * 当捅test接口时，可以看到，monitorProvider的实现类MonitorServiceImpl打印
 application:dubbo-consumer,service:com.xuzhaocai.dubbo.provider.IHelloProviderService,method:getName,group:null,version:null,client:null,server:192.168.1.106:18109,timestamp:1598709674608,success:1000,failure:0,input:0,output:36000,elapsed:842,concurrent:2,maxInput:0,maxOutput:36,maxElapsed:2,maxConcurrent:10

 application:dubbo-provider,service:com.alibaba.dubbo.monitor.MonitorService,method:collect,group:null,version:null,client:192.168.1.106,server:null,timestamp:1598709674609,success:3,failure:0,input:2179,output:0,elapsed:0,concurrent:0,maxInput:730,maxOutput:0,maxElapsed:0,maxConcurrent:1

 application:dubbo-provider,service:com.xuzhaocai.dubbo.provider.IHelloProviderService,method:getName,group:null,version:null,client:192.168.1.106,server:null,timestamp:1598709674609,success:1000,failure:0,input:244000,output:0,elapsed:1,concurrent:1,maxInput:244,maxOutput:0,maxElapsed:1,maxConcurrent:5

 application:dubbo-consumer,service:com.xuzhaocai.dubbo.provider.IHelloProviderService,method:getName,group:null,version:null,client:null,server:192.168.1.106:18109,timestamp:1598709734611,success:4000,failure:0,input:0,output:144000,elapsed:3457,concurrent:2,maxInput:0,maxOutput:36,maxElapsed:7,maxConcurrent:10

 application:dubbo-provider,service:com.alibaba.dubbo.monitor.MonitorService,method:collect,group:null,version:null,client:192.168.1.106,server:null,timestamp:1598709734614,success:4,failure:0,input:2947,output:0,elapsed:0,concurrent:0,maxInput:747,maxOutput:0,maxElapsed:0,maxConcurrent:1

 application:dubbo-provider,service:com.xuzhaocai.dubbo.provider.IHelloProviderService,method:getName,group:null,version:null,client:192.168.1.106,server:null,timestamp:1598709734614,success:4000,failure:0,input:976000,output:0,elapsed:6,concurrent:1,maxInput:244,maxOutput:0,maxElapsed:1,maxConcurrent:6

 */