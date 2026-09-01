package nano.engine.kernel;
import nano.engine.kernel.HttpRequestParser;

public class TestHttpRequestParser{
    private static byte[] standardGet = (
					 "GET /index.html HTTP/1.0\r\n" +
					 "Host: localhost\r\n" +
					 "Connection: close\r\n" +
					 "\r\n"
					 ).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    public static void main(String[] args){
	
	var parser = new HttpRequestParser();
	var parsedData = parser.parse(standardGet);
	System.out.println(parsedData);

    }
}
