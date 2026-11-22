package edu.csc223.planner;

import java.util.NoSuchElementException;
import java.util.Objects;

/** FIFO chain: size reachable nodes, null ends iff empty, tail.next null. */
public final class LinkedQueue<E> implements SimpleQueue<E> {
    private static class Node<E> {
        private final E element;
        private Node<E> next;
        private Node(E element) { this.element = element; }
    }
    private Node<E> head;
    private Node<E> tail;
    private int size;
    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public void enqueue(E element) {
        Node<E> added = new Node<>(element);
        if (tail == null) { head = added; }
        else { tail.next = added; }
        tail = added;
        size++;
    }
    public E peek() {
        requireNotEmpty();
        return head.element;
    }
    public E dequeue() {
        requireNotEmpty();
        Node<E> removed = head;
        head = removed.next;
        removed.next = null;
        size--;
        if (size == 0) { tail = null; }
        return removed.element;
    }
    public void clear() { head = null; tail = null; size = 0; }
    public boolean contains(E value) {
        for (Node<E> node = head; node != null; node = node.next) {
            if (Objects.equals(node.element, value)) { return true; }
        }
        return false;
    }
    /** Detached references in FIFO order; never exposes nodes. */
    public Object[] snapshot() {
        Object[] copy = new Object[size];
        int i = 0;
        for (Node<E> node = head; node != null; node = node.next) {
            copy[i++] = node.element;
        }
        return copy;
    }
    private void requireNotEmpty() {
        if (size == 0) { throw new NoSuchElementException("Queue is empty"); }
    }
}
