package nano.engine.kernel;

import nano.engine.kernel.BitRoute;




public class TestBitRoute {

    private static int  readIntFromByteArray(byte[] tape, int offset){
	int result = 0;
	for (int i =1; i<= 4;i++){
	    int t=  (tape[i + offset -1] & 0xFF) << (32 -  8*i);
	    //System.out.printf("i = %d, result = %d\n",t,result);
	    result |= t;
	}
	return result;
    } 

    
    public static void main(String[] argv){
	//System.out.println("Testing BitRoute");
	var bitRouter = new BitRoute(true, null);
	bitRouter.encode("/user/{id}/help/me".getBytes());
	bitRouter.encode("/user/home/free/me".getBytes());
	bitRouter.encode("/user/{id}".getBytes());
	bitRouter.encode("/user/{id}/must/be".getBytes());
	bitRouter.encode("/user/constant/must/be".getBytes());
	var tape = bitRouter.getEncodeTape();
	//System.out.printf("%d\n", tape[2]);
	assert(tape[2]==4);
	//System.out.printf("%d\n", tape[5]);
	assert(tape[12]==-1);
	//System.out.printf(" record size = %d, %d\n", tape[1], tape[0]);
	assert(32 == (int)(tape[1]));
	assert(24306 ==readIntFromByteArray(tape, 13));
	assert(10919 ==readIntFromByteArray(tape, 28));
	//	assert(bitRouter.getNextFreeOffset() == 32);

	assert(32 == (int)(tape[33]));
	//	assert(bitRouter.getNextFreeOffset() == 64);

	assert(tape[66] == 2);
	//assert(bitRouter.getNextFreeOffset() == 86);

	var symbols = bitRouter.getSymbolsArrayList();
	//System.out.printf("%d\n", symbols.size());
	assert(symbols.size() == 5);

	assert(symbols.get(3).size() == 4);
	assert(symbols.get(3).get(0).size() == 4);
	assert(symbols.get(3).get(1).size() == 1);
	assert(symbols.get(3).get(3).size() == 2);

	var compiledSymbols = bitRouter.getCompiledSymbosl();
	var bitRouterDecoder = new BitRoute(false, compiledSymbols);


	bitRouterDecoder.decode("/user/1/help/me".getBytes(),tape);
	int[]indices =  bitRouterDecoder.getMatchedRecordIndices();
	for (int i : indices){
	    System.out.println(i);
	}

	assert(bitRouterDecoder.getMatches().length == 5);
	assert(bitRouterDecoder.getMatches()[0] == 0);
	assert(bitRouterDecoder.getMatches()[1] == -1);

	bitRouterDecoder.decode("/user/constant/must/be".getBytes(), tape);
	indices =  bitRouterDecoder.getMatchedRecordIndices();
	for (int i : indices){
	    System.out.println(i);
	}

	//System.out.println(bitRouterDecoder.getMatchedRecordIndices().length);
	assert(bitRouterDecoder.getMatches().length == 5);
    }
}
