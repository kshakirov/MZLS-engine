package nano.engine.kernel;
import nano.engine.kernel.WirthHttpParser.STATUS;

import nano.engine.kernel.WirthHttpParser.BodyType;




import nano.engine.kernel.HttpBodyParserAutomaton;
import nano.engine.kernel.HttpBodyParserAutomaton.State;
import nano.engine.kernel.HttpBodyParserAutomaton.NetworkInput;

public class HttpRequestParser {
    public enum Phase {
	FINISHED,
	HEADER,
	BODY
	
    }
    public enum ParserState {
	START,
	FINISH,
	ERROR,
	NEEDS_MORE_DATA

    }


    private WirthHttpParser wirthHttpParser;
    private byte[] arena;
    private int consumedBytes; //index
    private int contentLength;
    private WirthHttpParser.STATUS headerStatus;
    private HttpBodyParserAutomaton automaton;
    private Phase phase;

    
    //    private 
    public HttpRequestParser(){
	this.wirthHttpParser = new WirthHttpParser();

	//	offsetTable = new int[64];
	arena = new byte[1024];

	consumedBytes =0;
	contentLength =0;
	headerStatus = STATUS.REQ_METHOD;
	phase = Phase.HEADER;
	this.automaton = new HttpBodyParserAutomaton(State.PARSE_HEADERS,
						     NetworkInput.HEADERS_PARSED_CONTENT_LENGTH,
						     contentLength,
						     null,
						     consumedBytes,
						     arena
														 
						     );
	
    };

    public long getCotentLength(){
	return this.contentLength;
    };


    public ParserState  parse(byte[] fragment){
	//somewhere to accumulate the whole body


	if(phase == Phase.HEADER){
	    headerStatus = wirthHttpParser.parse(fragment);
	    if(headerStatus!= STATUS.ERROR && headerStatus != STATUS.FINISHED){

		return ParserState.NEEDS_MORE_DATA;
	    }else if(headerStatus== STATUS.ERROR){
		return ParserState.ERROR;
	    }
	    if(headerStatus==STATUS.FINISHED && phase == Phase.HEADER){
		if(wirthHttpParser.getBodyType()== BodyType.FIXED_CONTENT){
		    contentLength = wirthHttpParser.getContentLength();
		    byte[] arena = new byte[contentLength];
		    automaton = new HttpBodyParserAutomaton(State.PARSE_HEADERS,
								 NetworkInput.HEADERS_PARSED_CONTENT_LENGTH,
								 contentLength,
								 fragment,
								 0,
								 arena
														 
								 );
	    
		    phase = Phase.BODY;   
		}

	    }
	}
	    

	if(phase == Phase.BODY){
	    automaton.resetBufferPointer();
	    automaton.resetArenaPointer();
	    this.arena = automaton.runEngine(fragment);
	    var state = automaton.getStatus();
	    if(state!= State.SUCCESS && state!= State.ERROR){
		
		return ParserState.NEEDS_MORE_DATA;
		
	    }
	}

    
	
	return ParserState.FINISH;
    }

    public byte[] getArena(){
	return this.arena;
    }

}
