package edu.csc223.planner;

import java.util.function.Predicate;

/** Private project storage: order-preserving dynamic array, never null items. */
final class SequentialStore<E> {
    private Object[] elements = new Object[2];
    private int size;
    int size() { return size; }
    @SuppressWarnings("unchecked")
    E get(int index) {
        check(index);
        return (E) elements[index];
    }
    void add(E item) {
        require(item);
        if (size == elements.length) {
            Object[] expanded = new Object[Math.multiplyExact(size, 2)];
            System.arraycopy(elements, 0, expanded, 0, size);
            elements = expanded;
        }
        elements[size++] = item;
    }
    E set(int index, E item) {
        check(index); require(item);
        E old = get(index);
        elements[index] = item;
        return old;
    }
    E remove(int index) {
        E old = get(index);
        System.arraycopy(elements, index + 1, elements, index, size - index - 1);
        elements[--size] = null;
        return old;
    }
    int find(Predicate<E> predicate) {
        for (int i = 0; i < size; i++) {
            if (predicate.test(get(i))) { return i; }
        }
        return -1;
    }
    private void check(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + ", size " + size);
        }
    }
    private void require(E item) {
        if (item == null) { throw new IllegalArgumentException("Record required"); }
    }
}
