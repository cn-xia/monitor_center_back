package org.hdu.crawler.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MonitorInfoUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(MonitorInfoUtil.class);
	
	private static boolean isLinux;
	
	private static final int CPUTIME = 100;
	
	private static final int PERCENT = 100;
	
	private static final int FAULTLENGTH = 10;
	
	@Value("${crawler.monitor.isLinux}")
	public void setLinux(boolean isLinux) {
		MonitorInfoUtil.isLinux = isLinux;
	}

	/**
	 * Linux下内存信息
	 * 
	 * @return
	 */
	public static String getMemMsg() {
		logger.info("开始收集memory使用率");
		float memUsage = 0.0f;
		if(!isLinux){ 
			try {
				OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory
				.getOperatingSystemMXBean();
				// 总的物理内存+虚拟内存
				long totalvirtualMemory = osmxb.getTotalSwapSpaceSize();
				// 剩余的物理内存
				long freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();
				Double usage = (Double) (1 - freePhysicalMemorySize * 1.0 / totalvirtualMemory);
				BigDecimal b1 = new BigDecimal(usage);
				memUsage = b1.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			} catch (Exception e) {
				logger.debug(e.getMessage());
			}
			//return "0.5";
		}else {
			Process pro = null;
			Runtime r = Runtime.getRuntime();
			try {
				String command = "cat /proc/meminfo";
				pro = r.exec(command);
				BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
				String line = null;
				int count = 0;
				long totalMem = 0, freeMem = 0;
				while ((line = in.readLine()) != null) {
					logger.info(line);
					String[] memInfo = line.split("\\s+");
					if (memInfo[0].startsWith("MemTotal")) {
						totalMem = Long.parseLong(memInfo[1]);
					}
					if (memInfo[0].startsWith("MemFree")) {
						freeMem = Long.parseLong(memInfo[1]);
					}
					memUsage = 1 - (float) freeMem / (float) totalMem;
					logger.info("本节点内存使用率为: " + memUsage);
					if (++count == 2) {
						break;
					}
				}
				in.close();
				pro.destroy();
			} catch (IOException e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.error("MemUsage发生InstantiationException. " + e.getMessage());
				logger.error(sw.toString());
			}
		}
		DecimalFormat df = new DecimalFormat("0.0000");  
		String result = df.format(memUsage);      
		return result;
	}

	/**
	 * linux下 Purpose:采集CPU使用率
	 * 
	 * @param args
	 * @return float,CPU使用率,小于1
	 */
	public static String getCpuMsg() {
		logger.info("开始收集cpu使用率");
		float cpuUsage = 0;
		if(!isLinux){ //windows cpu使用率
			try {
				String procCmd = System.getenv("windir")
				+ "//system32//wbem//wmic.exe process get Caption,CommandLine,KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
				// 取进程信息
				long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));//第一次读取CPU信息
				Thread.sleep(CPUTIME);//睡500ms
				long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));//第二次读取CPU信息
				if (c0 != null && c1 != null) {
					long idletime = c1[0] - c0[0];//空闲时间
					long busytime = c1[1] - c0[1];//使用时间
					Double cpusage = Double.valueOf(busytime * 1.0 / (busytime + idletime));
					BigDecimal b1 = new BigDecimal(cpusage);
					cpuUsage = b1.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
				} else {
					cpuUsage = 0;
				}
			} catch (Exception ex) {
				logger.debug(ex.getMessage());
				cpuUsage = 0;
			}
			//return "0.5";
		}else { //linux cpu使用率
			Process pro1, pro2;
			Runtime r = Runtime.getRuntime();
			try {
				String command = "cat /proc/stat";
				// 第一次采集CPU时间
				long startTime = System.currentTimeMillis();
				pro1 = r.exec(command);
				BufferedReader in1 = new BufferedReader(new InputStreamReader(pro1.getInputStream()));
				String line = null;
				long idleCpuTime1 = 0, totalCpuTime1 = 0; // 分别为系统启动后空闲的CPU时间和总的CPU时间
				while ((line = in1.readLine()) != null) {
					if (line.startsWith("cpu")) {
						line = line.trim();
						logger.info(line);
						String[] temp = line.split("\\s+");
						idleCpuTime1 = Long.parseLong(temp[4]);
						for (String s : temp) {
							if (!s.equals("cpu")) {
								totalCpuTime1 += Long.parseLong(s);
							}
						}
						logger.info("IdleCpuTime: " + idleCpuTime1 + ", " + "TotalCpuTime" + totalCpuTime1);
						break;
					}
				}
				in1.close();
				pro1.destroy();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					logger.error("CpuUsage休眠时发生InterruptedException. " + e.getMessage());
					logger.error(sw.toString());
				}
				// 第二次采集CPU时间
				long endTime = System.currentTimeMillis();
				pro2 = r.exec(command);
				BufferedReader in2 = new BufferedReader(new InputStreamReader(pro2.getInputStream()));
				long idleCpuTime2 = 0, totalCpuTime2 = 0; // 分别为系统启动后空闲的CPU时间和总的CPU时间
				while ((line = in2.readLine()) != null) {
					if (line.startsWith("cpu")) {
						line = line.trim();
						logger.info(line);
						String[] temp = line.split("\\s+");
						idleCpuTime2 = Long.parseLong(temp[4]);
						for (String s : temp) {
							if (!s.equals("cpu")) {
								totalCpuTime2 += Long.parseLong(s);
							}
						}
						logger.info("IdleCpuTime: " + idleCpuTime2 + ", " + "TotalCpuTime" + totalCpuTime2);
						break;
					}
				}
				if (idleCpuTime1 != 0 && totalCpuTime1 != 0 && idleCpuTime2 != 0 && totalCpuTime2 != 0) {
					cpuUsage = 1 - (float) (idleCpuTime2 - idleCpuTime1) / (float) (totalCpuTime2 - totalCpuTime1);
					logger.info("本节点CPU使用率为: " + cpuUsage);
				}
				in2.close();
				pro2.destroy();
			} catch (IOException e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.error("CpuUsage发生InstantiationException. " + e.getMessage());
				logger.error(sw.toString());
			}
		}	
		DecimalFormat df = new DecimalFormat("0.0000");
		String result = df.format(cpuUsage);      
		return result;
	}
	
	// window读取cpu相关信息
	private static long[] readCpu(final Process proc) {
		long[] retn = new long[2];
		try {
			proc.getOutputStream().close();
			InputStreamReader ir = new InputStreamReader(proc.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			String line = input.readLine();
			if (line == null || line.length() < FAULTLENGTH) {
				return null;
			}
			int capidx = line.indexOf("Caption");
			int cmdidx = line.indexOf("CommandLine");
			int rocidx = line.indexOf("ReadOperationCount");
			int umtidx = line.indexOf("UserModeTime");
			int kmtidx = line.indexOf("KernelModeTime");
			int wocidx = line.indexOf("WriteOperationCount");
			long idletime = 0;
			long kneltime = 0;//读取物理设备时间
			long usertime = 0;//执行代码占用时间
			while ((line = input.readLine()) != null) {
			if (line.length() < wocidx) {
				continue;
			}
			// 字段出现顺序：Caption,CommandLine,KernelModeTime,ReadOperationCount
			String caption = substring(line, capidx, cmdidx - 1).trim();
			//System.out.println("caption:"+caption);
			String cmd = substring(line, cmdidx, kmtidx - 1).trim();
			//System.out.println("cmd:"+cmd);
			if (cmd.indexOf("wmic.exe") >= 0) {
				continue;
			}
			String s1 = substring(line, kmtidx, rocidx - 1).trim();
			String s2 = substring(line, umtidx, wocidx - 1).trim();
			List<String> digitS1 = getDigit(s1);
			List<String> digitS2 = getDigit(s2);
		
			//System.out.println("s1:"+digitS1.get(0));
			//System.out.println("s2:"+digitS2.get(0));
		
			if (caption.equals("System Idle Process") || caption.equals("System")) {
				if (s1.length() > 0) {
					if (!digitS1.get(0).equals("") && digitS1.get(0) != null) {
						idletime += Long.valueOf(digitS1.get(0)).longValue();
					}
				}
				if (s2.length() > 0) {
					if (!digitS2.get(0).equals("") && digitS2.get(0) != null) {
						idletime += Long.valueOf(digitS2.get(0)).longValue();
					}
				}
				continue;
			}
			if (s1.length() > 0) {
				if (!digitS1.get(0).equals("") && digitS1.get(0) != null) {
					kneltime += Long.valueOf(digitS1.get(0)).longValue();
				}
				}
				if (s2.length() > 0) {
					if (!digitS2.get(0).equals("") && digitS2.get(0) != null) {
						kneltime += Long.valueOf(digitS2.get(0)).longValue();
					}
				}
				}
				retn[0] = idletime;
				retn[1] = kneltime + usertime;	
			return retn;
		} catch (Exception ex) {
			logger.debug(ex.getMessage());
		} finally {
			try {
				proc.getInputStream().close();
			} catch (Exception e) {
				logger.debug(e.getMessage());
			}
		}
		return null;
	}
	
	/**
	* 由于String.subString对汉字处理存在问题（把一个汉字视为一个字节)，因此在 包含汉字的字符串时存在隐患，现调整如下：
	* 
	* @param src
	* 要截取的字符串
	* @param start_idx
	* 开始坐标（包括该坐标)
	* @param end_idx
	* 截止坐标（包括该坐标）
	* @return
	*/
	private static String substring(String src, int start_idx, int end_idx) {
		byte[] b = src.getBytes();
		String tgt = "";
		for (int i = start_idx; i <= end_idx; i++) {
			tgt += (char) b[i];
		}
		return tgt;
	}
	
	/**
	* 从字符串文本中获得数字
	* 
	* @param text
	* @return
	*/
	private static List<String> getDigit(String text) {
		List<String> digitList = new ArrayList<String>();
		digitList.add(text.replaceAll("\\D", ""));
		return digitList;
	}
	
		public static void main(String[] args) throws Exception {
			String cpuUsage = getCpuMsg();
			//当前系统的内存使用率
			String memUsage = getMemMsg();
			/*//当前系统的硬盘使用率
			double diskUsage = ComputerMonitorUtil .getDiskUsage();*/
		
			System.out.println("cpuUsage:"+cpuUsage);
			System.out.println("memUsage:"+memUsage);
			//System.out.println("diskUsage:"+diskUsage);
	}

}