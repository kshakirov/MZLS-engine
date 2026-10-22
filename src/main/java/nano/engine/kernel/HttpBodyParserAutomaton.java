package nano.engine.kernel;



public class HttpBodyParserAutomaton {

    public enum  State {

	INITIAL,
	READ_CHUNK_SIZE,
	PREPARE_CHUNK_CR,
	EXPECT_CHUNK_CR,
	EXPECT_CHUNK_LF,
	EXPECT_FINAL_CR,
	EXPECT_FINAL_LF,
	PROCESS_CHUNK_LF,
	READ_FIXED_BODY,
	READ_CHUNK_DATA,
	SUCCESS,
	ERROR;
    }

    public enum Event{
	NO_BODY,
	FIXED_LENGTH_BODY,
	CHUNKED_BODY,
	NEED_MORE_DATA,
	CHUNK_SIZE_DIGIT_ACCEPTED,
	CHUNK_SIZE_OVERFLOW,
	ZERO_CHUNK_SIZE_DETECTED,
	ZERO_CHUNK_SIZE_TERMINATED,
	CHUNK_SIZE_TERMINATED,
	CR_ACCEPTED,
	LF_ACCEPTED,
	FIXED_BODY_BYTE_ACCEPTED,
	CHUNK_DATA_BYTE_ACCEPTED,
	ZERO_CHUNK_CR_REQUIRED,
	INVALID_INPUT,
	TIMEOUT;
    }

    public enum DelimiterContext {
	NO_DELIMITER,
	AFTER_SIZE,
	AFTER_DATA,
	AFTER_ZERO;
    }

    private State currentState;
    private Event currentEvent;
    private DelimiterContext delimiterContext;
    private int currentValue;

    private byte[] arena;                                                                      
    private int arenaPtr;


    public HttpBodyParserAutomaton(State state, Event event, int value,  byte[] arena){
	currentState = state;
	currentEvent = event;
	delimiterContext = DelimiterContext.NO_DELIMITER;
	currentValue = value;
	arenaPtr = 0;
	this.arena = arena;
    }
    
    private void nextState(){
	switch(currentState){

	case State.READ_FIXED_BODY: {
	    if (currentEvent == Event.FIXED_BODY_BYTE_ACCEPTED && currentValue > 0){

		currentState =  State.READ_FIXED_BODY;
		return;
	    }
	    else if (currentEvent == Event.FIXED_BODY_BYTE_ACCEPTED && currentValue ==0){
		
		currentState =  State.SUCCESS;
		return;
	    }
	    break;
	}
	case State.READ_CHUNK_DATA: {
	    if (currentEvent == Event.CHUNK_DATA_BYTE_ACCEPTED && currentValue > 0){
		currentState = State.READ_CHUNK_DATA;
		return;
	    }
	    else if (currentEvent == Event.CHUNK_DATA_BYTE_ACCEPTED && currentValue == 0){
		currentState = State.EXPECT_CHUNK_CR;
		return;
	    }
	    break;
	}

	case State.READ_CHUNK_SIZE:{

	    switch(currentEvent){

	    case Event.CHUNK_SIZE_DIGIT_ACCEPTED:{
		currentState = State.READ_CHUNK_SIZE;
		return;

	    }
	    case Event.CHUNK_SIZE_OVERFLOW	:{
		currentState = State.ERROR;
		return;
	    }

	    case Event.ZERO_CHUNK_SIZE_TERMINATED:{
		currentState = State.EXPECT_CHUNK_LF;
		delimiterContext = DelimiterContext.AFTER_ZERO;
		return;
	    }
	    case Event.CHUNK_SIZE_TERMINATED:{
		currentState= State.EXPECT_CHUNK_LF;
		delimiterContext = DelimiterContext.AFTER_SIZE;
		return;
	    }
	    case Event.ZERO_CHUNK_SIZE_DETECTED:{
		currentState = State.PREPARE_CHUNK_CR;
		currentEvent= Event.ZERO_CHUNK_CR_REQUIRED;
		return;

	    }

	    default:{
	    }
	    }
	    break;
	}


	case State.EXPECT_CHUNK_LF:{
	    switch(currentEvent){
	    case Event.LF_ACCEPTED:{
		switch(delimiterContext){
		case DelimiterContext.AFTER_SIZE:{
		    currentState = State.READ_CHUNK_DATA;
		    return;
		}
		case DelimiterContext.AFTER_DATA:{
		    currentState = State.READ_CHUNK_SIZE;
		    return;
		}
		case DelimiterContext.AFTER_ZERO:{
		    currentState = State.EXPECT_FINAL_CR;
		    return;
		}
		default: {
		    currentState = State.ERROR;
		    return;
		}
		}
	    }
	    default:{
		currentState = State.ERROR;
		return;
	    }
	    }
	}

	case State.EXPECT_CHUNK_CR:{
	    switch(currentEvent){
	    case Event.CR_ACCEPTED:{
		currentState = State.EXPECT_CHUNK_LF;
		delimiterContext = DelimiterContext.AFTER_DATA;
		return  ;
		}
		
	    default:{
		currentState = State.ERROR;
		return;
	    }
	    }
	}

	case State.EXPECT_FINAL_CR:{
	    if (currentEvent == Event.CR_ACCEPTED) {
		currentState = State.EXPECT_FINAL_LF;
		return;
	    }
	    currentState = State.ERROR;
	    return;
	}

	case State.EXPECT_FINAL_LF:{
	    if (currentEvent == Event.LF_ACCEPTED) {
		currentState = State.SUCCESS;
		return;
	    }
	    currentState = State.ERROR;
	    return;
	}

	 
	case PREPARE_CHUNK_CR:{
	    switch(currentEvent){
	    case ZERO_CHUNK_CR_REQUIRED:{
		currentState = State.EXPECT_CHUNK_CR;
		return;
	    }

	    default: {
		
	    }
	    }
	}
	default: {
	    
	}
	}
	
    }
    public State getStatus(){
	return this.currentState;
    }

    public int getArenPtr(){
	return this.arenaPtr;
    }
    
			   
    public byte[] runEngine(byte fragment){

	switch(currentState){
	case State.SUCCESS:{
	    return arena;
	}
	case State.ERROR:{
	    return arena;
	}
	case State.READ_FIXED_BODY :{
	    currentValue -= 1;
	    currentState = State.READ_FIXED_BODY;
	    currentEvent = Event.FIXED_BODY_BYTE_ACCEPTED;
	    arena[arenaPtr] = fragment;
	    arenaPtr += 1;
	    break;
	}
        case State.READ_CHUNK_SIZE:{
	    if(fragment  ==  13){
		currentEvent = currentValue == 0
		    ? Event.ZERO_CHUNK_SIZE_TERMINATED
		    : Event.CHUNK_SIZE_TERMINATED;
		
	    }else if(fragment > 30 && fragment < 100){ //here all possible values for hex
		int v = fromAsciiToHex(fragment);
		if(v > -1){
		    currentValue = currentValue * 16 + v;
		    currentEvent = Event.CHUNK_SIZE_DIGIT_ACCEPTED;
		}else{
		    currentState = State.ERROR;
		    break;
		}
	    }else{
		//error or malformed
	    }

	    break; 
	    
	    
	    }
	case State.READ_CHUNK_DATA :{
	    currentValue -= 1;
	    currentState = State.READ_CHUNK_DATA;
	    currentEvent = Event.CHUNK_DATA_BYTE_ACCEPTED;
	    arena[arenaPtr] = fragment;
	    arenaPtr += 1;
	    break;
	}
	case EXPECT_CHUNK_CR: {
	    if(fragment == 13) {
		currentEvent = Event.CR_ACCEPTED;
	    }else{
		currentEvent = Event.INVALID_INPUT;
	    }
	    break;
	}
	case EXPECT_CHUNK_LF:{
	    if(fragment == 10){
		currentEvent = Event.LF_ACCEPTED;
	    }else{
		currentEvent = Event.INVALID_INPUT;
	    }
	    break;
	}
	case EXPECT_FINAL_CR:{
	    currentEvent = fragment == 13
		? Event.CR_ACCEPTED
		: Event.INVALID_INPUT;
	    break;
	}
	case EXPECT_FINAL_LF:{
	    currentEvent = fragment == 10
		? Event.LF_ACCEPTED
		: Event.INVALID_INPUT;
	    break;
	}
	default: {

	    break;
	}
	      
	}
	nextState();

	return arena;// do we need it?
    }
    private int fromAsciiToHex(byte c){
	int v;
	if (c >= '0' && c <= '9') v = c - '0';
	else if (c >= 'A' && c <= 'F') v = c - 'A' + 10;
	else if (c >= 'a' && c <= 'f') v = c - 'a' + 10;
	else v = -1;
	return v;
    }
   
}
