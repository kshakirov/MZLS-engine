package nano.engine.kernel;

import nano.engine.kernel.WirthHttpParser;
public  class TestWirthHttpParser{
    private static byte[] standardGet = (
    "GET /index.html HTTP/1.0\r\n" +
    "Host: localhost\r\n" +
    "Connection: close\r\n" +
    "\r\n"
).getBytes(java.nio.charset.StandardCharsets.UTF_8);


    private byte[] shortGet = (
    "GET / HTTP/1.0\r\n" +
    "\r\n"
).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private byte[] heavyGet = (
    "GET /api/v1/users/profile?id=42 HTTP/1.1\r\n" +
    "Host: 127.0.0.1\r\n" +
    "User-Agent: wrk/4.2.0\r\n" +
    "Accept: */*\r\n" +
    "X-Real-IP: 192.168.1.100\r\n" +
    "Connection: keep-alive\r\n" +
    "\r\n"
).getBytes(java.nio.charset.StandardCharsets.UTF_8);

    public static void main(String[] args){
	var console= System.console();
	System.out.println("Hello");
	var parser = new WirthHttpParser();
	var result = parser.parse(standardGet);
	console.printf("%d,%d\n",result[0],result[1]);
    }
     
}
