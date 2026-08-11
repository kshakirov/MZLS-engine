package nano.engine.kernel;

import nano.engine.kernel.WirthHttpParser;
public  class TestWirthHttpParser{
    private static byte[] standardGet = (
					 "GET /index.html HTTP/1.0\r\n" +
					 "Host: localhost\r\n" +
					 "Connection: close\r\n" +
					 "\r\n"
					 ).getBytes(java.nio.charset.StandardCharsets.UTF_8);


    private static byte[] shortGet = (
			       "GET /tell HTTP/1.0\r\n" +
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
    public static void dumpOffsets(byte[] payload, int[] offsets) {
    String[] names = {"METHOD", "URI", "VERSION"};
    
    for (int j = 0; j < names.length; j++) {
        int start = offsets[j * 2];
        int end = offsets[j * 2 + 1];
        int length = end - start + 1; // +1, так как энд-индекс у тебя включительный
        
        System.out.print(start + "__ " + end + "__ " + j+ " _" + names[j] + ": [");
        // Печатаем сырые байты прямо в консоль как символы
        System.out.write(payload, start, length);
        System.out.println("]");
    }
}


    public static void main(String[] args){
	var console= System.console();
	System.out.println("Hello");
	var parser = new WirthHttpParser();
	//	var result = parser.parse(standardGet);
	var result = parser.parse(shortGet);
	dumpOffsets(shortGet, result);
    
    }
     
}
