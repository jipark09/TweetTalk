package util;

import java.io.Closeable;
import java.io.IOException;

public class IOUtil {
//    public static final String Host = "192.168.0.237";
    public static final String Host = "127.0.0.1";
    public static final int PORT = 10001;
    
    public static void allClose(Closeable ... closeables) {
        try {
            for(Closeable temp : closeables) {
                if(temp != null) {
                    temp.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
