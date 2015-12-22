package nl.idgis.geoide.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test cases for the {@link IndexedRingBuffer} class: asserts that various aspects of the contract
 * function properly. 
 */
public class IndexedRingBufferTest {

	/**
	 * Asserts proper operation of the index ring buffer when repeatedly adding items to a buffer
	 * of length 1:
	 * - old items should be rotated away.
	 * - The base index should be incremented for each item.
	 */
	@Test
	public void testAddSize1 () {
		final IndexedRingBuffer<Integer> buffer = new IndexedRingBuffer<> (1);
		
		buffer.add (42);
		
		assertEquals (0, buffer.getBaseIndex ());
		assertEquals (42, (int)buffer.get (0).get ());
		
		buffer.add (43);
		
		assertEquals (1, buffer.getBaseIndex ());
		assertFalse (buffer.get (0).isPresent ());
		assertEquals (43, (int)buffer.get (1).get ());
		
		buffer.add (44);
		
		assertEquals (2, buffer.getBaseIndex ());
		assertFalse (buffer.get (0).isPresent ());
		assertFalse (buffer.get (1).isPresent ());
		assertEquals (44, (int)buffer.get (2).get ());
		
		buffer.add (45);
		
		assertEquals (3, buffer.getBaseIndex ());
		assertFalse (buffer.get (0).isPresent ());
		assertFalse (buffer.get (1).isPresent ());
		assertFalse (buffer.get (2).isPresent ());
		assertEquals (45, (int)buffer.get (3).get ());
	}
	
	/**
	 * Asserts proper operation of the index ring buffer when repeatedly adding items to a buffer
	 * of length 2:
	 * - old items should be rotated away.
	 * - The base index should be incremented for each item.
	 */
	@Test
	public void testAddSize2 () {
		final IndexedRingBuffer<Integer> buffer = new IndexedRingBuffer<> (2);
		
		buffer.add (42);
		
		assertEquals (0, buffer.getBaseIndex ());
		assertEquals (42, (int)buffer.get (0).get ());
		
		buffer.add (43);
		
		assertEquals (0, buffer.getBaseIndex ());
		assertEquals (42, (int)buffer.get (0).get ());
		assertEquals (43, (int)buffer.get (1).get ());
		
		buffer.add (44);
		
		assertEquals (1, buffer.getBaseIndex ());
		assertFalse (buffer.get (0).isPresent ());
		assertEquals (43, (int)buffer.get (1).get ());
		assertEquals (44, (int)buffer.get (2).get ());
		
		buffer.add (45);
		
		assertEquals (2, buffer.getBaseIndex ());
		assertFalse (buffer.get (0).isPresent ());
		assertFalse (buffer.get (1).isPresent ());
		assertEquals (44, (int)buffer.get (2).get ());
		assertEquals (45, (int)buffer.get (3).get ());
	}

	/**
	 * Asserts that a ring buffer of size zero cannot be created.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testRingBufferZeroSize () {
		new IndexedRingBuffer<> (0);
	}
	
	/**
	 * Asserts that a ring buffer of negative size cannot be created.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testRingBufferNegativeSize () {
		new IndexedRingBuffer<> (-1);
	}

	/**
	 * Asserts that getting an item at a negative index always fails.
	 */
	@Test (expected = IndexOutOfBoundsException.class)
	public void testGetNegative () {
		final IndexedRingBuffer<Integer> buffer = new IndexedRingBuffer<> (5);
		
		buffer.add (42);
		buffer.add (43);
		buffer.add (42);
		
		buffer.get (-1);
	}

	/**
	 * Asserts that getting an item from a ring buffer of non-zero size to which
	 * no items have been added fails.
	 */
	@Test (expected = IndexOutOfBoundsException.class)
	public void testGetEmptyBuffer () {
		final IndexedRingBuffer<Integer> buffer = new IndexedRingBuffer<> (5);
		
		buffer.get (0);
	}

	/**
	 * Asserts that requesting an item from a partially filled ring buffer fails. This
	 * happens when less items have been added than the length of the ring buffer.
	 */
	@Test (expected = IndexOutOfBoundsException.class)
	public void testGetBeyondMaximumPartiallyFilled () {
		final IndexedRingBuffer<Integer> buffer = new IndexedRingBuffer<> (5);

		buffer.add (42);
		buffer.add (43);
		buffer.add (44);
		
		buffer.get (3);
	}

	/**
	 * Asserts that fetching an item at an index beyond the highest added item results
	 * in an exception.
	 */
	@Test (expected = IndexOutOfBoundsException.class)
	public void testGetBeyondMaximumFullBuffer () {
		final IndexedRingBuffer<Integer> buffer = new IndexedRingBuffer<> (5);

		buffer.add (42);
		buffer.add (43);
		buffer.add (44);
		buffer.add (45);
		buffer.add (46);
		buffer.add (47);
		
		buffer.get (6);
	}
	
	/**
	 * Asserts that old values in the ring buffer are overwritten when repeatedly adding new items to it.
	 */
	@Test
	public void testOverwriteBuffer () {
		final IndexedRingBuffer<Integer> buffer = new IndexedRingBuffer<> (5);
		
		for (int i = 0; i < 100; ++ i) {
			buffer.add (i);
		}
		
		for (int i = 0; i < 95; ++ i) {
			assertFalse ("Item " + i + " must be discarded by the buffer", buffer.get (i).isPresent ());
		}
		
		assertEquals (95, buffer.getBaseIndex ());
		assertEquals (95, buffer.get (95).get ().intValue ());
		assertEquals (96, buffer.get (96).get ().intValue ());
		assertEquals (97, buffer.get (97).get ().intValue ());
		assertEquals (98, buffer.get (98).get ().intValue ());
		assertEquals (99, buffer.get (99).get ().intValue ());
	}
}
