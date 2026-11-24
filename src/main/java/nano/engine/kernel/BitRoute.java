package nano.engine.kernel;


import java.util.ArrayList;

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

    public BitRoute (boolean encode,int records){
	this.state = State.SegmentStart;
	this.segmentCount =0;
	this.recordSize =0;
	if (encode){
	    this.encodeTape = new byte[128];
	    this.symbols = new ArrayList<>();
	}else{
	    this.decodeTape = new byte[128];
	    this.matchedRecordOffsets = new int[records];
	    for(int i =0; i < records;i++){
		this.matchedRecordOffsets[i] = -1;
	    }
	}
	this.nextFreeOffset=0;
	
    }
    private State state;
    private byte segmentCount;
    private int nextFreeOffset;
    private long[]  mask;
    private int recordSize;
    private byte[] encodeTape;
    private byte[] decodeTape;
    private int segmentStart =0;
    private int segmentEnd =0;
    private int recordHeaderEnd = 12;
    private int[] matchedRecordOffsets;
    public byte[] getEncodeTape() {
	return encodeTape;
    }
    private ArrayList<ArrayList<ArrayList<Byte>>> symbols;
    public int getNextFreeOffset(){
	return this.nextFreeOffset;
    }
    public int[] getMatches(){
	return this.matchedRecordOffsets;
    }
    public ArrayList<ArrayList<ArrayList<Byte>>> getSymbolsArrayList(){
	return this.symbols;
    }
    public void decode(byte[] url, byte[] compiledTape){
	code( url, decodeTape);
	int decode_segment_count = this.decodeTape[2];

	int currentEncodedRecordIndex =0;
	var nextRecordOffset = 0;
	var moreRecords = true;
	while (nextRecordOffset < compiledTape.length  && moreRecords) {
	    boolean bingo = true;
	   
	    //	    int rs = readRecordSize(compiledTape[nextRecordOffset], compiledTape[nextRecordOffset + 1]);
	    int rs = (int) compiledTape[nextRecordOffset + 1];
	    if(rs > 0){
		//System.out.printf("The record size is %d\n", rs);

		var current_segment_count = compiledTape[nextRecordOffset + 2];
	
		if(current_segment_count == decode_segment_count){
		    // System.out.printf("Segments current %d vs decode url %d\n", current_segment_count, decode_segment_count);
		    for(int i =0;i < decode_segment_count;i++){
			var decodeMask = decodeTape[12 + i*5];
			var currentMask =  compiledTape[nextRecordOffset + 12 + i *5];
			//	System.out.printf("Mask current %d vs decode Mask %d\n", currentMask, decodeMask);
			var decodeHash = readIntFromByteArray(decodeTape, 13 + i * 5);
			var currentHash = readIntFromByteArray(compiledTape,nextRecordOffset + 13 + i * 5 );
			if((currentMask & decodeHash) == (currentMask & currentHash)){

			    //  	    System.out.printf("Hashes AND with masks match: %d = %d\n", currentHash, decodeHash);
			}else{
			    //System.out.printf("Hashes do not match encoded %d vs decoded %d\n", currentHash, decodeHash);
			    bingo = false;
			    break;
			}
		    }
		}else{
		    bingo =false;
		}
		if(bingo){
		    this.matchedRecordOffsets[currentEncodedRecordIndex]= nextRecordOffset;
		    //System.out.printf("Matches record with offset %d\n", current_record_offset);
		}
		nextRecordOffset = nextRecordOffset + rs;
	
		//System.out.printf("Current jump to %d, tape length %d\n ", nextRecordOffset, compiledTape.length);
	    }else{
		moreRecords = false;
	    }
	    currentEncodedRecordIndex += 1;   
	}
    }

    public void encode(byte[] url){
	code(url, encodeTape);
	System.out.println("");
    }
    private  void code(byte[] url, byte[] tape) {
	
	this.segmentCount = 0;
	for (int i = 0; i < url.length; i++){
	    //System.out.printf("%s %c\n", state , url[i]);
	    switch(url[i]){
		
	    case 47 : {
		switch (state){
		case SegmentStart:{
		   

		    //write toe byte arraay
		    // System.out.println("Here");
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
		    writeIntToByteArray(hash,hash_offset,tape);
		    //    System.out.printf("sc = %d, mask_offset=%d, hash %d, hash_offset = %d\n", segmentCount,mask_offset, hash, hash_offset);
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
		    //System.out.printf("Parameter: sc = %d, mask_offset=%d\n", segmentCount,mask_offset);
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
	    writeIntToByteArray(hash,hash_offset,tape);
	    //System.out.printf("The end: Segment sc = %d, mask_offset=%d, hash %d, hash_offset = %d\n", segmentCount,mask_offset, hash, hash_offset);
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
	    //System.out.printf("The end: Parameter: sc = %d, mask_offset=%d\n", segmentCount,mask_offset);
	    tape[mask_offset] = mask;

	    
	}
	short recordSize  = (short) (recordHeaderEnd + segmentCount * 5);
	//System.out.printf("record size = %d\n", recordSize);
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

    private void writeIntToByteArray(int word, int offset, byte[] tape){
	for (int i =1; i<= 4;i++){
	    tape[i + offset - 1] = (byte) (word >> (32 -  8*i));   
	}
    }

    private int readRecordSize(byte left, byte right){
	//System.out.printf("l is %d, r is %d\n", left,right);
	int rs = right << 8;
	rs |= left;
	return rs;
    }

    private  int  readIntFromByteArray(byte[] tape, int offset){
	int result = 0;
	for (int i =1; i<= 4;i++){
	    int t=  (tape[i + offset -1] & 0xFF) << (32 -  8*i);
	    //System.out.printf("i = %d, result = %d\n",t,result);
	    result |= t;
	}
	return result;
    }

    public void encodeSymbols(byte[] url, int routeIndex){
	ArrayList<ArrayList<Byte>> symbolsByteArray = new ArrayList<>();
	var skip = false; 
	ArrayList<Byte> temp= null;
	for (byte u : url ){
	    switch(u){
	    case  47: {
		if(temp!= null){
		    symbolsByteArray.add(temp);
		}
		temp = new ArrayList<Byte>();
		break;
	    }
	    case 123: {
		skip = true;
		temp.add((byte) 123);
		break;
	    }
	    case 125:{
		skip = false;
		break;
	    }
	    default: {
		if(!skip){
		    temp.add(u);
		}
	    }
		
	    }
	}
	if(temp.size() > 0){
	    symbolsByteArray.add(temp);
	}
	this.symbols.add(symbolsByteArray);
    }
    
}
