package pawelwanat.net.common;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.ParseException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public final class ByteProcessor<INPUT, OUTPUT> {

	public static class NextInstanceResolvingException extends RuntimeException {

		private static final long serialVersionUID = -3007170255019670529L;
	}

	private boolean hasNextOnInput = false;
	private int nextToCheckPosition = 0;

	private final Deserializator<INPUT> inputProcessor;
	private final Serializator<OUTPUT> outputProcessor;
	private final ByteBuffer inputBuffer;
	private final int maxAccumulated;
	private final Queue<ByteBuffer> byteBufferQueue = new ArrayBlockingQueue<ByteBuffer>(5); 
	
	int accumulated;

	@Inject
	public ByteProcessor(
			Deserializator<INPUT> inputProcessor,
			Serializator<OUTPUT> outputProcessor,
			@Named("InputBuffer") ByteBuffer inputBuffer,
			@Named("MaxAccumulated") int maxAccumulated) {
		this.inputProcessor = inputProcessor;
		this.outputProcessor = outputProcessor;
		this.inputBuffer = inputBuffer;
		this.maxAccumulated = maxAccumulated;
		
		this.accumulated = 0;
	}

	public boolean hasBytesToSend() {
		// Is there some content in outputBuffer?
		return !byteBufferQueue.isEmpty();
	}

	/**
	 * @return number of bytes written to channel, possibly zero.
	 */
	public int nextToOutput(WritableByteChannel writableByteChannel) throws IOException {
		ByteBuffer bb = byteBufferQueue.element();
		int result = writableByteChannel.write(bb);
		if (bb.remaining() == 0) {
			byteBufferQueue.poll();
		}
		accumulated -= result;
        return result;
	}

	public void updateOutput(OUTPUT instance) {
		ByteBuffer serialized = outputProcessor.toByteBuffer(instance);
		accumulated += serialized.remaining();
		if (accumulated > maxAccumulated) {
			throw new BufferOverflowException();
		}
		byteBufferQueue.add(serialized);
	}

	/**
	 * @return number of bytes read from channel, possibly zero; -1 if EOS reached.
	 */
	public int updateInput(ReadableByteChannel readableByteChannel) throws IOException {
		if(inputBuffer.position() == inputBuffer.capacity()){
			throw new BufferOverflowException();
		}
		return readableByteChannel.read(inputBuffer);
	}

	public boolean hasNextOnInput() throws ParseException {
		if(hasNextOnInput){
			return true;
		}
        int position = nextToCheckPosition, limit = inputBuffer.position();
		while (position < limit) {
			if (inputProcessor.process(inputBuffer.get(position++))) {
				nextToCheckPosition = position;
				hasNextOnInput = true;
				return true;
			}
		}
        nextToCheckPosition = limit;
        return false;
	}

	INPUT nextFromInput() throws ParseException {
		if(hasNextOnInput()){
			// Setting position and limit to mark INPUT instance.
			inputBuffer.flip();
			int limit = inputBuffer.limit();
			inputBuffer.limit(nextToCheckPosition);
			// Extracting instance as buffer of bytes and then obtaining real instance.
			ByteBuffer readOnlyRawInstance = inputBuffer.slice().asReadOnlyBuffer();
			INPUT instance = inputProcessor.getInstance(readOnlyRawInstance);
			// Fixing position for Buffer, moving content to front and setting position and limit
			// for new content.
			inputBuffer.position(nextToCheckPosition);
			inputBuffer.limit(limit);
			inputBuffer.compact();
			// Clearing information about having next ready.
			hasNextOnInput = false;
			nextToCheckPosition = 0;

			return instance;
		}
		throw new NextInstanceResolvingException();
	}
}
