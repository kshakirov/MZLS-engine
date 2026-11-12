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
	this.nextFreeOffset=0;
	
    }
    private State state;
    private byte segmentCount;
    private int recordSize;
    private int nextFreeOffset;
    private long[]  mask;
    private byte[] tape;
    private int segmentStart =0;
    private int segmentEnd =0;
    private int recordHeaderEnd = 12;
    public byte[] getTape() {
	return tape;
    }
    public int getNextFreeOffset(){
	return this.nextFreeOffset;
    }
    public  void decode(byte[] url){
	
	this.segmentCount = 0;
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
		    segmentEnd = i;
		    var hash = hash16(url,segmentStart, segmentEnd);
		    var mask_offset = nextFreeOffset +   segmentCount * 5 + recordHeaderEnd ;
		    var mask =(byte) 0xFFFF;
		    var hash_offset = mask_offset + 1;
		    writeIntToByteArray(hash,hash_offset);
		    System.out.printf("sc = %d, mask_offset=%d, hash %d, hash_offset = %d\n", segmentCount,mask_offset, hash, hash_offset);
		    tape[mask_offset] = mask;
		    
		    state = State.SegmentStart;
		    segmentCount += 1;
		    tape[nextFreeOffset + 2]=segmentCount;


		    break;

		}
		case State.ParameterEnd: {
		    state = State.SegmentStart;
		    segmentCount += 1;
		    tape[nextFreeOffset + 2]=segmentCount;

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
		    var mask_offset =  nextFreeOffset + segmentCount * 5 + recordHeaderEnd ;
		    var mask =(byte) 0x0;
		    System.out.printf("Parameter: sc = %d, mask_offset=%d\n", segmentCount,mask_offset);
		    tape[mask_offset] = mask;
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
	    var hash = hash16(url,segmentStart, url.length); 
	    var mask_offset =  nextFreeOffset + segmentCount * 5 + recordHeaderEnd ;
	    var mask =(byte) 0xFFFF;
	    var hash_offset = mask_offset + 1;
	    writeIntToByteArray(hash,hash_offset);
	    System.out.printf("The end: Segment sc = %d, mask_offset=%d, hash %d, hash_offset = %d\n", segmentCount,mask_offset, hash, hash_offset);
	    tape[mask_offset] = mask;
	    segmentCount += 1;
	    tape[nextFreeOffset + 2]=segmentCount;
	    state = State.SegmentEnd;

	}else if(state== State.ParameterEnd){
	    segmentCount += 1;
	    tape[nextFreeOffset + 2] = segmentCount;
	    state = State.SegmentEnd;
	}else if(state== State.FillingParameter){
	    var mask_offset =  nextFreeOffset + segmentCount * 5 + recordHeaderEnd ;
	    var mask =(byte) 0x0;
	    System.out.printf("The end: Parameter: sc = %d, mask_offset=%d\n", segmentCount,mask_offset);
	    tape[mask_offset] = mask;

	    
	}
	short recordSize  = (short) (recordHeaderEnd + segmentCount * 5);
	System.out.printf("record size = %d\n", recordSize);
	tape[nextFreeOffset] = (byte) (recordSize >> 8);
	tape[nextFreeOffset + 1] = (byte) recordSize;
	nextFreeOffset += recordSize;
	this.state = State.SegmentStart;
	
	
    }
    private int hash16(byte[] data, int start, int end) {
	int hash = 0x811C9DC5;

	for (int i = start; i < end; i++) {
	    hash ^= data[i] & 0xFF;
	    hash *= 0x01000193;
	}

	return hash & 0xFFFF;
    }

    private void writeIntToByteArray(int word, int offset){
	for (int i =1; i<= 4;i++){
	    this.tape[i + offset - 1] = (byte) (word >> (32 -  8*i));   
	}
    }
    
}
