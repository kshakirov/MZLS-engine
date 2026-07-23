package nano.engine.kernel;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;


public class SimpleHttpReqParser{
    private static Pattern pattern;
    
    private static Pattern method ;
    private static Pattern headerPair;

    private static String[] getHeaders(byte[] body){
	if(pattern == null){
	    System.out.println("Lazy init Pattern");
	    pattern = Pattern.compile("\r\n");
	    method = Pattern.compile("(?<method>GET|POST /<url.+>HTTP.*)");
	    headerPair = Pattern.compile(":");
		
	}
	
	return pattern.split(new String(body));
    }
    public static String[] isFinished(ByteArrayOutputStream outStream){
	var pattern = new byte[]{'\r','\n', '\r','\n'};
	var array = outStream.toByteArray();
	for(int i = array.length - 4; i > 0; i--){
	    //здесь можно реализовать красивый поис со сдвигом но пока небуде пока брут форсе
	    System.out.printf("%c",array[i]);
	    if(pattern[0] == array[i] &&
	       pattern[1] == array[i+1] &&
	       pattern[2] == array[i+2] &&
	       pattern[3] == array[i+3]){
		var headers = getHeaders(array);
		return headers;
	    }
	}

	return null ;
    }
    public static byte[] isOK(String[] parsedReq){
	StringBuilder builder = new StringBuilder();
	for (String str: parsedReq){
	    builder.append(str);
	}
	int length = builder.toString().getBytes().length;
	String body = builder.toString();
	String content = String.format("Content-Length: %d\r\n",length);
	String[] okResp = {"HTTP/1.1 200 OK\r\n",
			   "Content-Type: text/html; charset=UTF-8\r\n",
			   content,
			   "\r\n",
			   body};

	builder = new StringBuilder();
	for (String str:okResp){
	    builder.append(str);
	}
	return builder.toString().getBytes();
    }
    
}
