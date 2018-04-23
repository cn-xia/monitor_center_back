package org.hdu.back.util;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class FileUtil {

	public static String getEncoding(InputStream inputStream) throws Exception{  

        BufferedInputStream bin = new BufferedInputStream(inputStream);  
        int p = (bin.read() << 8) + bin.read();  
        String code = null;  
        switch (p) {  
            case 0xefbb:  
                code = "UTF-8";  
                break;  
            case 0xfffe:  
                code = "Unicode";  
                break;  
            case 0xfeff:  
                code = "UTF-16BE";  
                break;  
            default:  
                code = "GBK";  
        }          
        return code;  
    }  
	
}
