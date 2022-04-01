package com.alibaba.dubbo.examples.gpmonitor.consumer.impl;

import com.alibaba.dubbo.examples.gpmonitor.consumer.IHelloProviderService;

@org.springframework.stereotype.Service
@com.alibaba.dubbo.config.annotation.Service
public class IHelloProviderServiceImpl  implements IHelloProviderService {

	@Override
	public String getName(Integer id) {
		return "test";
	}
}
/** 这是xml中的配置
 <dubbo:monitor  address="dubbo://127.0.0.1:18109"></dubbo:monitor>
 <dubbo:application name="dubbo-provider">
 <dubbo:parameter key="qos.enable" value="true"/>
 <dubbo:parameter key="qos.accept.foreign.ip" value="false"/>
 <dubbo:parameter key="qos.port" value="8109"/>
 </dubbo:application>
 <dubbo:registry protocol="zookeeper" address="127.0.0.1:2181"/>
 <dubbo:protocol accesslog="true" name="dubbo" port="18109" />

 */