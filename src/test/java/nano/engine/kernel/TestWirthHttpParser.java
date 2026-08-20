package nano.engine.kernel;


import java.nio.charset.StandardCharsets;

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
    private static byte[] heavyGet = (
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


	    System.out.print( names[j] + ": [");
	    // Печатаем сырые байты прямо в консоль как символы
	    System.out.write(payload, start, length);
	    System.out.println("]");
	
	}

    
    }
    public static void dumpHeader(byte[] payload, int start, int end){
	System.out.printf("start %d end %d\n", start, end);
	int length = end - start;
	String str = new String(payload, start, length, StandardCharsets.UTF_8);
	System.out.println(str);
    }



    public static void main(String[] args){
	var console= System.console();
	System.out.println("Hello");
	var parser = new WirthHttpParser();
	var result = parser.parse(standardGet);
	System.out.printf("length payload %d \n", result.length);
		
	//var result = parser.parse(shortGet);
	dumpOffsets(standardGet, result);
	dumpHeader(standardGet, result[6], result[7]);
	dumpHeader(standardGet, result[8], result[9]);
	dumpHeader(standardGet, result[10], result[11]);
	// dumpHeader(heavyGet, result[9], result[10]);
	// dumpHeader(heavyGet, result[11], result[12]);
    
    }
     
}
