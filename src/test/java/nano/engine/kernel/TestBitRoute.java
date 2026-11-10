package nano.engine.kernel;

import nano.engine.kernel.BitRoute;




public class TestBitRoute {

    private static int  readIntFromByteArray(byte[] tape, int offset){
	int result = 0;
	for (int i =1; i<= 4;i++){
	    int t=  (tape[i + offset -1] & 0xFF) << (32 -  8*i);
	    System.out.printf("i = %d, result = %d\n",t,result);
	    result |= t;
	}
	return result;
    }

    
    public static void main(String[] argv){
	System.out.println("Testing BitRoute");
	var bitRouter = new BitRoute();
	bitRouter.parseUrl("/user/{id}/help/me".getBytes());
	var tape = bitRouter.getTape();
	System.out.printf("%d\n", tape[2]);
	assert(tape[2]==4);
	System.out.printf("%d\n", tape[5]);
	assert(tape[12]==-1);
	System.out.printf(" record size = %d, %d\n", tape[1], tape[0]);
	assert(36 == (int)(tape[1]));
	assert(24306 ==readIntFromByteArray(tape, 13));
	assert(10919 ==readIntFromByteArray(tape, 28));
	
    }
}
