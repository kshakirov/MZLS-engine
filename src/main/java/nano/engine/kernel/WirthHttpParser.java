package nano.engine.kernel;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.nio.ByteBuffer;

public class WirthHttpParser{
    public enum STATUS {
	REQ_START(0),
	REQ_METHOD(1),
	REQ_URI(2),
	REQ_VERSION(3),
	HEADER_START(4),
	HEADER_NAME(5),
	HEADER_VALUE(6),
	FINISHED   (7);
	STATUS(int value) {this.value=value;};
	private final int value;
	public int value() {return value;}
    }
    public enum METHODS{
	GET("GET"),
	HEAD("HEAD"),
	POST("POST"),
	PUT("GET");
	METHODS(String value){this.value=value;}
	private final String value;
	public String value() {return value;}
	public byte[] bValue() {return value.getBytes();}
    }
    
    public void  parse(ByteArrayOutputStream stream){
	var payload = stream.toByteArray();
	Map<String, String> httpHeader= new HashMap<String, String>();
     
	var status = STATUS.REQ_START;
	//	var current_byte = 0;
	for (int i=0; i < payload.length; i++){
	    //  current_byte = payload[i];
	    
	    switch(status){
	    case STATUS.REQ_START: {
		//no spaces allowed here must be POST,GET and so on
		//jump to 
		status = STATUS.REQ_METHOD;
		
	    };
		break;
	    case STATUS.REQ_METHOD: {
		if(payload.length < 3){
		    status = STATUS.REQ_METHOD;
		    break;
		}
		if(payload.length==3){
		    var method = isGetOrPut(payload);
		    if(method!=null){
			//do something
			httpHeader.put("method", sliceByteArray(0,3,payload ));
		    }else {
			break;
		    }
			
		}
		
	    };
	    case STATUS.REQ_URI: {
		
	    };
	    case STATUS.REQ_VERSION: {
		
	    };
	    case STATUS.HEADER_START: {
		
	    };
	    case STATUS.HEADER_NAME: {
		
	    };
	    case STATUS.HEADER_VALUE: {
		
	    };
	    case STATUS.FINISHED: {
		
	    };
		
	       
	    }
	    
	}
	
    }
    private METHODS isGetOrPut(byte[] payload){
	//we know exactly the possible length of method
	int methodLength = 3;
	ByteBuffer sliceBuffer = ByteBuffer.wrap(payload, 0, methodLength).slice();
	
	if(Arrays.equals(sliceBuffer.array(), METHODS.GET.bValue())){
	    return METHODS.GET;
	}
	else if(Arrays.equals(sliceBuffer.array(), METHODS.PUT.bValue())){
	    return METHODS.PUT;
	}
	return null;
    }

    private String sliceByteArray(int start, int end, byte[] payload){
	ByteBuffer sliceBuffer = ByteBuffer.wrap(payload, start, end).slice();
	return sliceBuffer.toString();
    }

}
