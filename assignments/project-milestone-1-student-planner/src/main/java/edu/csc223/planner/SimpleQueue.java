package edu.csc223.planner;

public interface SimpleQueue<E> {
    int size();
    boolean isEmpty();
    void enqueue(E element);
    E dequeue();
    E peek();
    void clear();
}
