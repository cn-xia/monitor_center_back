package org.hdu.back.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdUtil {
	
	public static void main(String[] args) {
		String result = execCmd("java -jar SearchCrawler-1.0-SNAPSHOT.jar", new File("./crawler"));
		System.out.println(result);
	}

    /**
     * 执行系统命令
     * @param cmd 需要执行的命令
     * @param dir 执行命令的子进程的工作目录, null 表示和当前主进程工作目录相同
     */
    public static String execCmd(String cmd, File dir){
        //执行命令, 返回一个子进程对象（命令在子进程中执行）
        // 方法阻塞, 等待爬虫初始化成功
		System.out.println(System.getProperty("user.dir"));
        try {
        	final Process process = Runtime.getRuntime().exec(cmd, null, dir);
        	
        	new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
			        String in;
					BufferedReader bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		            while((in=bReader.readLine()) != null){
		            	System.out.println(in);
		            }
		        	BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		        	 while((in=reader.readLine()) != null){
			            	System.out.println(in);
			        }
		        	System.out.println(process.isAlive());
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
        	process.waitFor();
			Thread.sleep(3000);
            if(process.isAlive()) {
            	return "启动爬虫成功，后台程序会实时监控爬虫";
            }else {
                return "启动爬虫失败,爬虫因某些原因自行关闭或被杀死";
            }
		} catch (Exception e) {
			e.printStackTrace();
			return "启动爬虫失败,详细信息：" + e.getMessage();
		}
    }

}
