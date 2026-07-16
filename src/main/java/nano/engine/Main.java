package nano.engine;
import nano.engine.kernel.*;
import java.io.IOException;

public  class Main {
  public static void main(String[] argv){
    System.out.println("hello");
    var server = new TestSocketServer("/tmp/.jbc_socket");
      try{
		server.run();
	    }
       
	    catch(IOException e){
		System.out.printf("an error is %s\n", e.getMessage());
	    }

  }
}
