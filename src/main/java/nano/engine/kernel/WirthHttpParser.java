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
	FINISHED   (7),

	REQ_URI_START(8);
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
    
    public int[]  parse(byte[] payload){
	//	var payload = stream.toByteArray();
	var status = STATUS.REQ_START;
	var console = System.console();
	var methodStart=0;
	var methodEnd=0;
	var uriStart=0;
	var uriEnd =0;
	var versionStart =0;
	var versionEnd=0;
	var ddos = true;
	var header_name_start_ptr = 0;
	var header_name_end_ptr = 0;
	var header_value_start_ptr = 0;
	var header_value_end_ptr = 0;
	
	var offsets = new int[128];// 127 method, uri,headers should be enough 
	for (int i=0; i < payload.length; i++){
	    //  current_byte = payload[i];
	
		
	    switch(status){
	    case STATUS.REQ_START: {
		//no spaces allowed here must be POST,GET and so on
		//jump to 
		status = STATUS.REQ_METHOD;
		methodStart = i;

		    
	    };
		break;
	    case STATUS.REQ_METHOD: {
		//console.printf("REQ_METHOD: [%c]\n",payload[i]);
		if(i==0 && i !=0x20){
		   
		}
		if(i==3){
		    var method = isGetOrPut(payload);
		    if(method){
			//do something
			//methodEnd = i;
			console.printf("STATUS.REQ_METHOD FOUND\n");
			offsets[0] = methodStart; //first offset for method
			methodEnd= i;
			offsets[1] = methodEnd;
			ddos =false;
			status = STATUS.REQ_URI;
		    }else if(i==4) {
			
		    }else if(i==5){

		    }
		    else if(i==6){
			
		    }else if(i==7){

		    }else if(i> 7){
			//something wrong 
		    }
		    
		    
		}
		    
		    
	    }
	
	    case STATUS.REQ_URI_START: {
		if(payload[i]==0x20){
		    console.printf("STATUS.REQ_URI_START FOUND\n");
		    status = STATUS.REQ_URI;
		    uriStart = i + 1;
		    offsets[2] = uriStart;
		}
		break;
		
	    }
	    case STATUS.REQ_URI: {
		if(payload[i]!=0x20){
		    uriEnd = i;
		    offsets[3]= uriEnd;
		    
		}else{
		    console.printf("STATUS.REQ_URI FOUND\n");
		    status = STATUS.REQ_VERSION;
		    versionStart = i + 1;
		    offsets[4] = versionStart;
		}
		break;
	    }
		
	    case STATUS.REQ_VERSION: {
		if(payload[i]!=0x0D){
		   
		    versionEnd = i;
		}else if(payload[i]==0x0D){
		     console.printf("STATUS.REQ_VERSOIN FOUND\n");
		     offsets[5]=versionEnd;
		     status = STATUS.HEADER_START;
		};
		break;
		
	    }

	    case STATUS.HEADER_START: {
		if(payload[i]==0x0A){
		    console.printf("STATUS.HEADER_START FOUND\n");
		    status = STATUS.HEADER_NAME;
		    header_name_start_ptr = i +1;
		}
		break;
		
	    }
	    case STATUS.HEADER_NAME: {
		if(payload[i]!=0x3A){

		    //status = STATUS.HEADER_NAME;
		    header_name_end_ptr = i;
		}else{
		    console.printf("STATUS.HEADER_NAME FOUND  \n");
		    header_value_start_ptr = i + 1;
		    status= STATUS.HEADER_VALUE;
		   
		}   

		break;
		
	    }
	    case STATUS.HEADER_VALUE: {
		if(payload[i]!=0x0A) {
		    header_value_end_ptr = i;
		}else{
		    console.printf("STATUS.HEADER_VALUE FOUND\n");
		    status = STATUS.HEADER_NAME;

		}
		break;
		
	    }
	    case STATUS.FINISHED: {
		break;
	    }
	    }
	

	       
	}
	    
	return offsets;
	
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
	    if(template[i]!=candidate[candiadateOffset + i]){
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
