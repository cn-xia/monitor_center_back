package org.hdu.crawler.monitor;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

@Component
public class MonitorThread extends Thread{
	
	public static boolean flag ; //是否监控
	
	private int state = -1;        
	 
	private MonitorParam monitorParam;
	
	private int interval = 10;//默认10分钟  
	
	@Resource
	private MonitorRequester moRequester;
	
	public MonitorThread(){
	}
	
	public MonitorThread(int state,MonitorParam monitorParam) {
		this.state = state;
		this.monitorParam = monitorParam;
	}
	
	public MonitorThread(int interval) {
		this.state = 1;
		this.interval = interval;
	}
	
	@Override
	public void run() {
		switch(state){
			case 0: //就绪
				String result = moRequester.start(monitorParam);
				if("-1".equals(result)){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					moRequester.start(monitorParam); //发生错误再次尝试请求
				}
				break;
			case 1: //运行
				while(flag){
					try {
						Thread.sleep(interval*60*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(!flag){
						break;
					}
					monitorParam = MonitorExecute.getMsgParam();//每次更新参数
					moRequester.sendMessage(monitorParam);
				}
				break;
			case 2:
				moRequester.sendMessage(monitorParam); //结束时最后一次调msg
				break;
			case 3: //结束
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				moRequester.sendDaily(monitorParam);//日报接口
				break;
			case 4:
				MonitorRequester.sendException(monitorParam);//错误通知接口
		}
	}

	
}
