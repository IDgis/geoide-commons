package nl.idgis.geoide.util;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * A ring buffer implementation that keeps track of the index of every item ever inserted.
 * The ring buffer has a fixed size, adding items when the buffer is full discards items from
 * the front of the buffer.
 * 
 * @param <T>	The type of the items in the buffer.
 */
public class IndexedRingBuffer<T> {
	private final int bufferSize;
	private final ArrayList<T> items;
	private int head = 0;
	private int tail = 0;
	private int size = 0;
	private long baseIndex = 0;
	
	/**
	 * Constructs a new {@link IndexedRingBuffer} with the given maximum size. Size must be > 0.
	 * 
	 * @param bufferSize	The buffer size, must be > 0.
	 */
	public IndexedRingBuffer (final int bufferSize) {
		if (bufferSize <= 0) {
			throw new IllegalArgumentException ("bufferSize must be >= 1");
		}
		
		this.bufferSize = bufferSize;
		this.items = new ArrayList<> (bufferSize);
	}

	/**
	 * Appends an item to the end of the buffer. If the buffer is at full capacity, items at the front
	 * of the buffer are rotated out. When this is the case, the base index of the buffer is
	 * incremented.
	 * 
	 * @param item	The item to add
	 */
	public void add (final T item) {
		Objects.requireNonNull (item, "item cannot be null");

		while (items.size () < head) {
			items.add (null);
		}

		if (head == items.size ()) {
			items.add (item);
		} else {
			items.set (head, item);
		}

		// Increment the tail:
		if (size == bufferSize && head == tail) {
			tail = (tail + 1) % bufferSize;
			++ baseIndex;
		}
		
		// Increment the head:
		head = (head + 1) % bufferSize;
		
		// Increment the fill count if the buffer hasn't been completely filled:
		if (size < bufferSize) {
			++ size;
		}
	}

	/**
	 * Returns an item in the buffer at the given index. If the item is no longer available because
	 * it is rotated out, an empty optional is returned. If a negative index is requested or an index
	 * beyond the last item inserted, an {@link IndexOutOfBoundsException} is thrown.
	 * 
	 * @param index		The index of the item to retrieve.
	 * @return			An optional containing the item, or an empty optional if the item has been rotated out.
	 */
	public Optional<T> get (final long index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException ("index should be >= 0");
		}
		if (index < baseIndex) {
			// The index is no longer available:
			return Optional.empty ();
		}
		if (index - baseIndex >= bufferSize) {
			throw new IndexOutOfBoundsException ("Index " + index + " too large");
		}
		if ((index - baseIndex) > bufferSize) {
			throw new IndexOutOfBoundsException ("Index " + index + " too large");
		}
		
		final int localIndex = (tail + (int)(index - baseIndex)) % bufferSize;

		
		return Optional.of (items.get (localIndex));
	}

	/**
	 * Returns the current base index of the buffer: the index of the first item that is still available in the buffer.
	 * 
	 * @return	The current base index of the buffer.
	 */
	public long getBaseIndex () {
		return baseIndex;
	}

	/**
	 * Returns the current buffer size. The buffer size will start at zero and increase until the maximum
	 * size for the buffer has been reached, then the buffer size will always equal the maximum size.
	 * 
	 * @return	The current buffer size.
	 */
	public int getBufferSize () {
		return bufferSize;
	}
}
