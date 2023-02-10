package Demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

/**
 * @ClassName Utils
 * @Description
 * @Author 15014
 * @Time 2023/2/9 17:41
 * @Version 1.0
 */
public class Utils {
    static Properties properties = new Properties();
    static File file = new File("setting.conf");
    static File logFile = new File("historyLog.txt");
    static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a");
    static Writer writer;
    static String whiteSpace = " \r\n\t";

    static {
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            writer = new FileWriter(logFile, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void log(String log) throws IOException {
        String prefixTime = dateFormat.format(new Date());
        writer.write(prefixTime + " " + trimDuplicateSpace(log));
        writer.flush();
    }

    public static String trimDuplicateSpace(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        char cache = '1';
        for (int i = 0; i < s.length(); i++) {
            char temp = s.charAt(i);
            if (whiteSpace.indexOf(temp) == -1) {
                cache = '0';
            } else {
                if (temp == cache) {
                    continue;
                }
                cache = temp;
            }
            stringBuilder.append(temp);
        }
        return stringBuilder.toString();
    }

    public static void init() {
        load();
    }

    public static void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Reader reader = new FileReader(file);
            properties.load(reader);
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void save() {
        try {
            Writer writer = new FileWriter(file);
            properties.store(writer, "属性列表");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File base64ToFile(String base64) {
        FileOutputStream fileOutputStream = null;
        try {
            File temp = File.createTempFile(UUID.randomUUID().toString(), "jpeg");
            fileOutputStream = new FileOutputStream(temp);
            fileOutputStream.write(Base64.getDecoder().decode(base64));
            return temp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        // 创建一个Buffer字符串
        byte[] buffer = new byte[6024];
        // 每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len = 0;
        // 使用一个输入流从buffer里把数据读取出来
        while ((len = inStream.read(buffer)) != -1) {
            // 用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        // 关闭输入流
        inStream.close();
        // 把outStream里的数据写入内存
        return outStream.toByteArray();
    }

    public static void writeFile(File file, byte[] bytes) throws IOException {
        FileOutputStream outStream = new FileOutputStream(file);
        outStream.write(bytes);
        outStream.close();
    }
}
