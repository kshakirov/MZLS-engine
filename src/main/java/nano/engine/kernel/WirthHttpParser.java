package nano.engine.kernel;

import java.io.ByteArrayOutputStream;


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
    
    public void  parse(ByteArrayOutputStream stream){
	var payload = stream.toByteArray();
	var status = STATUS.REQ_START;
	var current_byte = 0;
	for (int i=0; i < payload.length; i++){
	    current_byte = payload[i];
	    switch(status){
	    case STATUS.REQ_START: {
		
	    };
	    case STATUS.REQ_METHOD: {
		
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

}
