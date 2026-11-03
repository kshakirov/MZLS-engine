package nano.engine.kernel;



public class BitRoute {

    
    public enum State{
	SegmentStart,
	FillingSegment,
	SegmentEnd,
	ParameterStart,
	FillingParameter,
	ParameterEnd,
	Error
    }

    public BitRoute (){
	this.state = State.SegmentStart;
	this.segmentCount =0;
	this.recordSize=0;
	this.tape = new byte[128];
	
    }
    private State state;
    private byte segmentCount;
    private int recordSize;
    private long[]  mask;
    private byte[] tape;
    private int segmentStart =0;
    private int segmentEnd =0;
    public byte[] getTape() {
	return tape;
    }
    public  void parseUrl(byte[] url){
	
	
	for (int i = 0; i < url.length; i++){
	    System.out.printf("%s %c\n", state , url[i]);
	    switch(url[i]){
		
	    case 47 : {
		switch (state){
		case SegmentStart:{
		   

		    //write toe byte arraay
		    System.out.println("Here");
		    break;
		}
		case State.FillingSegment:{
		    //write to bytearray the hash
		    //by segment offset
		    segmentEnd = i - 1;
		    var hash = hash16(url,segmentStart, segmentEnd);
		    state = State.SegmentStart;
		    segmentCount += 1;
		    tape[2]=segmentCount;
		    break;

		}
		case State.ParameterEnd: {
		    state = State.SegmentStart;
		    segmentCount += 1;
		    tape[2]=segmentCount;

		}
		
		default :{
		    break;
		}

		}
		break;
		
	    }
	    case 123: {
		switch (state){
		case SegmentStart:{
		    state = State.FillingParameter;
		    break;
		}
		    
		default:{
		    break;
		}
		}
		break;
		    
	    }

	    case 125: {
		if(state==State.FillingParameter){
		    //write mask
		    //write segment hash 0
		    //we forgot about value of parameter
		    state = State.ParameterEnd;
		}else{

		    

		}
		break;
	    }
	    default: {
		switch(state){
		case State.SegmentStart:{
		    state = State.FillingSegment;
		    segmentStart = i;
		    recordSize += 1;
		    //we can write directly to byte array
		    break;
		}
		case State.FillingParameter:{
		    segmentStart = i;
		    recordSize += 1;
		    break;
		}
		default: {
		    break;
		}
		}
		break;
	    }

	    }
	};
	if(state == State.FillingSegment){
	    segmentCount += 1;
	    tape[2]=segmentCount;
	    state = State.SegmentEnd;

	}
	
    }
    private int hash16(byte[] data, int start, int end) {
	int hash = 0x811C9DC5;

	for (int i = start; i < end; i++) {
	    hash ^= data[i] & 0xFF;
	    hash *= 0x01000193;
	}

	return hash & 0xFFFF;
    }
    
}
