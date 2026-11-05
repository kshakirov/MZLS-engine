package nano.engine.kernel;

import nano.engine.kernel.BitRoute;




public class TestBitRoute {



    public static void main(String[] argv){
	System.out.println("Testing BitRoute");
	var bitRouter = new BitRoute();
	bitRouter.parseUrl("/user/{id}/help/me".getBytes());
	var tape = bitRouter.getTape();
	System.out.printf("%d\n", tape[2]);
	assert(tape[2]==4);
	System.out.printf("%d\n", tape[8]);
	assert(tape[8]==-1);
	
	
    }
}
