package edu.csc223.planner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.NoSuchElementException;
import java.util.IdentityHashMap;

class LinkedQueueTest {
    private Object field(Object object, String name) {
        try {
            var f = object.getClass().getDeclaredField(name);
            f.setAccessible(true); return f.get(object);
        } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
    }
    private void invariant(LinkedQueue<?> q) {
        Object head = field(q, "head"), tail = field(q, "tail"), last = null;
        assertTrue(q.size() >= 0);
        if (q.isEmpty()) { assertNull(head); assertNull(tail); }
        else { assertNotNull(head); assertNotNull(tail); }
        var seen = new IdentityHashMap<Object, Boolean>();
        for (Object node = head; node != null; node = field(node, "next")) {
            assertNull(seen.put(node, true)); last = node;
        }
        assertEquals(q.size(), seen.size()); assertSame(last, tail);
        if (tail != null) { assertNull(field(tail, "next")); }
        if (q.size() == 1) { assertSame(head, tail); }
    }
    @Test void newQueueIsEmpty() {
        var q = new LinkedQueue<String>(); assertEquals(0, q.size()); invariant(q);
    }
    @Test void singleInsertionAndNonMutatingPeek() {
        var q = new LinkedQueue<String>(); q.enqueue("A");
        assertEquals("A", q.peek()); assertEquals("A", q.peek());
        assertEquals(1, q.size()); invariant(q);
    }
    @Test void fourValuesAreRemovedInFifoOrder() {
        var q = new LinkedQueue<Integer>();
        for (int i = 1; i <= 4; i++) { q.enqueue(i); invariant(q); }
        for (int i = 1; i <= 4; i++) {
            assertEquals(Integer.valueOf(i), q.dequeue());
            assertEquals(4 - i, q.size()); invariant(q);
        }
    }
    @Test void repeatedSingletonDepletionAndReuse() {
        var q = new LinkedQueue<Integer>();
        for (int i = 0; i < 100; i++) {
            q.enqueue(i); invariant(q);
            assertEquals(Integer.valueOf(i), q.dequeue()); invariant(q);
        }
    }
    @Test void clearResetsBothEndsAndAllowsReuse() {
        var q = new LinkedQueue<String>(); q.enqueue("A"); q.enqueue("B");
        q.clear(); invariant(q); q.enqueue("C");
        assertEquals("C", q.dequeue()); invariant(q);
    }
    @Test void emptyFailuresDoNotChangeState() {
        var q = new LinkedQueue<String>();
        assertThrows(NoSuchElementException.class, q::peek);
        assertThrows(NoSuchElementException.class, q::dequeue);
        invariant(q); q.enqueue("A"); assertEquals("A", q.dequeue()); invariant(q);
    }
    @Test void nullIsSupportedByGenericQueue() {
        var q = new LinkedQueue<String>(); q.enqueue(null); q.enqueue("A");
        assertTrue(q.contains(null)); assertNull(q.peek()); assertNull(q.dequeue());
        assertEquals(1, q.size()); assertEquals("A", q.peek()); invariant(q);
    }
    @Test void snapshotIsDetachedAndRemovedLinkIsCleared() {
        var q = new LinkedQueue<String>(); q.enqueue("A"); q.enqueue("B");
        var copy = q.snapshot(); copy[0] = "X"; assertEquals("A", q.peek());
        Object old = field(q, "head"); q.dequeue(); assertNull(field(old, "next"));
        assertArrayEquals(new Object[]{"B"}, q.snapshot()); invariant(q);
    }
    @Test void interleavedEnqueueAndDequeuePreserveEnds() {
        var q = new LinkedQueue<String>(); q.enqueue("A"); q.enqueue("B");
        q.dequeue(); q.enqueue("C"); assertEquals("B", q.dequeue());
        assertEquals("C", q.dequeue()); q.enqueue("D");
        assertEquals("D", q.peek()); invariant(q);
    }
}
