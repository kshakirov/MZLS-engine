package nano.engine.kernel;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import nano.engine.kernel.HttpBodyParserAutomaton.Event;
import nano.engine.kernel.HttpBodyParserAutomaton.State;

public class TestHttpBodyParserAutomaton {

    public static void main(String[] args) {
	check("A\r\n0123456789\r\n0\r\n\r\n", "0123456789");
	check("3\r\nabc\r\n5\r\ndefgh\r\n0\r\n\r\n", "abcdefgh");
	checkError("1\rX");
	checkError("\n");
	checkError("1\r\naX");
	checkError("0\r\nX");
	checkError("0\r\n\rX");

	System.out.println("Chunked body automaton scenarios: OK");
    }

    private static void check(String encodedBody, String expectedBody) {
	byte[] input = encodedBody.getBytes(StandardCharsets.US_ASCII);
	byte[] expected = expectedBody.getBytes(StandardCharsets.US_ASCII);
	byte[] arena = new byte[64];
	var automaton = new HttpBodyParserAutomaton(
	    State.READ_CHUNK_SIZE,
	    Event.NEED_MORE_DATA,
	    0,
	    arena
	);

	for (int i = 0; i < input.length; i++) {
	    automaton.runEngine(input[i]);
	    if (i < input.length - 1) {
		assert automaton.getStatus() != State.SUCCESS;
	    }
	}

	assert automaton.getStatus() == State.SUCCESS;
	assert automaton.getArenPtr() == expected.length;
	assert Arrays.equals(
	    Arrays.copyOf(arena, automaton.getArenPtr()),
	    expected
	);
    }

    private static void checkError(String encodedBody) {
	byte[] arena = new byte[64];
	var automaton = new HttpBodyParserAutomaton(
	    State.READ_CHUNK_SIZE,
	    Event.NEED_MORE_DATA,
	    0,
	    arena
	);

	for (byte b : encodedBody.getBytes(StandardCharsets.US_ASCII)) {
	    automaton.runEngine(b);
	}

	assert automaton.getStatus() == State.ERROR;
    }
}
