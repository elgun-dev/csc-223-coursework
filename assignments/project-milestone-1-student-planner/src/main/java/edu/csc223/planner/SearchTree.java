package edu.csc223.planner;

import java.util.List;

/** Ordered unique elements; comparison equality defines duplicates.
 * Null arguments to add, contains and remove throw IllegalArgumentException.
 * Stored ordering keys must remain stable while in the tree. */
public interface SearchTree<E> {
    /** Returns the number of stored elements. */
    int size();
    /** Returns true exactly when size is zero. */
    boolean isEmpty();
    /** Adds a leaf; returns false without changing state for a duplicate. */
    boolean add(E element);
    /** Returns whether a comparison-equivalent element exists. */
    boolean contains(E element);
    /** Removes a matching element; returns false unchanged if absent. */
    boolean remove(E element);
    /** Returns the least element; throws NoSuchElementException if empty. */
    E minimum();
    /** Returns the greatest element; throws NoSuchElementException if empty. */
    E maximum();
    /** Edge height: empty is -1 and a leaf is 0. */
    int height();
    /** Returns a new root-left-right list. */
    List<E> preorder();
    /** Returns a new left-root-right list in ascending order. */
    List<E> inorder();
    /** Returns a new left-right-root list. */
    List<E> postorder();
    /** Returns a new breadth-first list, left before right. */
    List<E> levelOrder();
    /** Resets the tree to its empty state; safe when already empty. */
    void clear();
}
