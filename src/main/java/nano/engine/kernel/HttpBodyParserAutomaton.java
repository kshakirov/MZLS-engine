package nano.engine.kernel;


public class HttpBodyParserAutomaton {

    public enum  State {

	INITIAL,
	READ_CHUNK_SIZE,
	PREPARE_CHUNK_CR,
	EXPECT_CHUNK_CR,
	EXPECT_CHUNK_LF,
	PROCESS_CHUNK_LF,
	READ_BODY,
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
	BODY_BYTE_ACCEPTED,
	ZERO_CHUNK_CR_REQUIRED,
	INVALID_INPUT,
	TIMEOUT;
    }
    private State currentState;
    private Event currentEvent;
    private int currentValue;

    private byte[] arena;                                                                      
    private int arenaPtr;


    public HttpBodyParserAutomaton(State state, Event event, int value,  byte[] arena){
	currentState = state;
	currentEvent = event;
	currentValue = value;
	arenaPtr = 0;
	this.arena = arena;
    }
    
    private void nextState(){
	switch(currentState){

	case State.READ_BODY: {
	    if (currentEvent == Event.BODY_BYTE_ACCEPTED && currentValue > 0){

		currentState =  State.READ_BODY;
		return;
	    }
	    else if (currentEvent == Event.BODY_BYTE_ACCEPTED && currentValue ==0){
		
		currentState =  State.SUCCESS;
		return;
	    }
	    break;
	}

	case State.READ_CHUNK_SIZE:{

	    switch(currentEvent){

	    case Event.CHUNK_SIZE_DIGIT_ACCEPTED:{
		currentState = State.READ_CHUNK_SIZE;

	    }
	    case Event.CHUNK_SIZE_OVERFLOW	:{
		currentState = State.ERROR;
	    }

	    case Event.ZERO_CHUNK_SIZE_TERMINATED:{
		currentState = State.EXPECT_CHUNK_LF;   
	    }
	    case Event.CHUNK_SIZE_TERMINATED:{
		currentState= State.EXPECT_CHUNK_LF;
	    }
	    case Event.ZERO_CHUNK_SIZE_DETECTED:{
		currentState = State.PREPARE_CHUNK_CR;
		currentEvent= Event.ZERO_CHUNK_CR_REQUIRED;
		return;

	    }

	    default:{
	    }
	    }
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
	case State.READ_BODY :{
	    currentValue -= 1;
	    currentState = State.READ_BODY;
	    currentEvent = Event.BODY_BYTE_ACCEPTED;
	    arena[arenaPtr] = fragment;
	    arenaPtr += 1;
	   
	}
	case EXPECT_CHUNK_CR: {
	    switch(currentEvent){
	    case ZERO_CHUNK_CR_REQUIRED:{
		//as far as we read by one byte we don't need to check its existence
		if(fragment == 13){
		    //we don't take it to arena but redirect to further reading of
		    currentState= State.EXPECT_CHUNK_LF;
		    
		}else{
		    currentState = State.ERROR;
		    return arena;
		}
	    }
	    default:{
	    }
	    }
	    
	    
	}
	default: {

	    break;
	}
	      
	}
	nextState();

	return arena;
    }
   
}
