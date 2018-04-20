package org.hdu.util;

import java.io.File;
import java.io.IOException;

public class CmdUtil {

    /**
     * 执行系统命令
     * @param cmd 需要执行的命令
     * @param dir 执行命令的子进程的工作目录, null 表示和当前主进程工作目录相同
     */
    public static String execCmd(String cmd, File dir){
        try {
            //执行命令, 返回一个子进程对象（命令在子进程中执行）
            Process process = Runtime.getRuntime().exec(cmd, null, dir);
            // 方法阻塞, 等待命令执行完成（成功会返回0）
            if(process.isAlive()) {
                return "启动爬虫成功，后台程序会实时监控爬虫";
            }else {
                return "启动爬虫失败,爬虫因某些原因自行关闭或被杀死";
            }
        }catch (IOException e){
            return "启动爬虫失败，详细信息：" + e.getMessage();
        }
    }

}
