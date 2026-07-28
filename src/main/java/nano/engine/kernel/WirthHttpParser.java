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
	var status = STATUS.REQ_START;
	var start =0;
	var ddos = true;
	var offsets = new int[128];// 127 method, uri,headers should be enough 
	for (int i=0; i < payload.length; i++){
	    //  current_byte = payload[i];
	    if(payload[i]==0x20){
		
		switch(status){
		case STATUS.REQ_START: {
		    //no spaces allowed here must be POST,GET and so on
		    //jump to 
		    status = STATUS.REQ_METHOD;
		    ddos = false;
		    
		};
		    break;
		case STATUS.REQ_METHOD: {
		    if(i==3){
			var method = isGetOrPut(payload);
			if(method){
			    //do something
			    start = i;
			    offsets[0] = i; //first offset for method
			    
			}else {
			    break;
			}
		    
		    }
		    
		    
		}
	
		
		case STATUS.REQ_URI: {
		    break;
		}
		case STATUS.REQ_VERSION: {
		    break;
		
		}
		case STATUS.HEADER_START: {
		    break;
		
		}
		case STATUS.HEADER_NAME: {
		    break;
		
		}
		case STATUS.HEADER_VALUE: {
		    break;
		
		}
		case STATUS.FINISHED: {
		    break;
		}
		}
	    }else if(i > 8 && ddos){
		//just return for the moment later we'll see
		return ;
	    }else{
		//do something just skip
		
	    }

	       
	}
	    
	
	
    }
    private boolean isGetOrPut(byte[] payload){
	//we know exactly the possible length of method
	if(fullMatch(METHODS.GET.bValue(),payload, 0)){
	    return true;
	}
	if(fullMatch(METHODS.PUT.bValue(),payload, 0)){
	    return true;
	}

		
	return false;
    }

    private boolean fullMatch(byte[] template, byte[] candidate, int candiadateOffset){
	var length = template.length;
	for (int i =0;i< length;i++){
	    if(template[i]==candidate[candiadateOffset + i]){
		return false;
	    }
	}
	return true;
    }
    

    private String sliceByteArray(int start, int end, byte[] payload){
	ByteBuffer sliceBuffer = ByteBuffer.wrap(payload, start, end).slice();
	return sliceBuffer.toString();
    }

}
