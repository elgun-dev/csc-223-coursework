package edu.csc223.planner;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/** Unbalanced linked BST. Contracts and null policy are in SearchTree. */
public class BinarySearchTree<E extends Comparable<? super E>>
        implements SearchTree<E> {
    private static final class Node<E> {
        private E element;
        private Node<E> left;
        private Node<E> right;
        private Node(E element) { this.element=element; }
    }
    private Node<E> root;
    private int size;
    public int size() { return size; }
    public boolean isEmpty() { return size==0; }
    private void require(E element) {
        if(element==null) throw new IllegalArgumentException("Null element");
    }
    public boolean add(E element) {
        require(element);
        if(root==null) { root=new Node<>(element);size++;return true; }
        Node<E> current=root;
        while(true) {
            int comparison=element.compareTo(current.element);
            if(comparison==0) return false;
            if(comparison<0) {
                if(current.left==null) {
                    current.left=new Node<>(element);break;
                }
                current=current.left;
            } else {
                if(current.right==null) {
                    current.right=new Node<>(element);break;
                }
                current=current.right;
            }
        }
        size++;return true;
    }
    public record Lookup<T>(java.util.Optional<T> match,int comparisons) { }
    /** Returns the stored value and actual comparator calls along one path. */
    public Lookup<E> lookup(E key) {
        require(key);Node<E> current=root;int comparisons=0;
        while(current!=null) {
            int comparison=key.compareTo(current.element);comparisons++;
            if(comparison==0)
                return new Lookup<>(java.util.Optional.of(current.element),comparisons);
            current=comparison<0?current.left:current.right;
        }
        return new Lookup<>(java.util.Optional.empty(),comparisons);
    }
    public boolean contains(E element) { return lookup(element).match().isPresent(); }
    /** Replaces only an equivalent key, preserving shape; returns old value.
     * Missing key returns empty. Null or changed keys are rejected. */
    public java.util.Optional<E> replace(E key,E replacement) {
        require(key);require(replacement);
        if(key.compareTo(replacement)!=0)
            throw new IllegalArgumentException("Ordering key cannot change");
        Node<E> current=root;
        while(current!=null) {
            int comparison=key.compareTo(current.element);
            if(comparison==0) {
                E old=current.element;current.element=replacement;
                return java.util.Optional.of(old);
            }
            current=comparison<0?current.left:current.right;
        }
        return java.util.Optional.empty();
    }
    /** Inclusive ordered range. Reversed bounds throw IllegalArgumentException. */
    public List<E> range(E lower,E upper) {
        require(lower);require(upper);
        if(lower.compareTo(upper)>0) throw new IllegalArgumentException("Reversed range");
        List<E> result=new ArrayList<>();range(root,lower,upper,result);return result;
    }
    private void range(Node<E> node,E lower,E upper,List<E> result) {
        if(node==null) return;
        int low=lower.compareTo(node.element),high=upper.compareTo(node.element);
        if(low<0) range(node.left,lower,upper,result);
        if(low<=0 && high>=0) result.add(node.element);
        if(high>0) range(node.right,lower,upper,result);
    }
    public boolean remove(E element) {
        require(element);Node<E> parent=null;Node<E> current=root;
        while(current!=null) {
            int comparison=element.compareTo(current.element);
            if(comparison==0) break;
            parent=current;current=comparison<0?current.left:current.right;
        }
        if(current==null) return false;
        if(current.left!=null && current.right!=null) {
            // Copy successor, then unlink its original node. It has no left child.
            Node<E> successorParent=current;
            Node<E> successor=current.right;
            while(successor.left!=null) {
                successorParent=successor;successor=successor.left;
            }
            current.element=successor.element;
            parent=successorParent;current=successor;
        }
        Node<E> child=current.left!=null?current.left:current.right;
        if(parent==null) root=child;
        else if(parent.left==current) parent.left=child;
        else parent.right=child;
        size--;return true;
    }
    public E minimum() {
        if(root==null) throw new NoSuchElementException("Empty tree");
        Node<E> current=root;
        while(current.left!=null) current=current.left;
        return current.element;
    }
    public E maximum() {
        if(root==null) throw new NoSuchElementException("Empty tree");
        Node<E> current=root;
        while(current.right!=null) current=current.right;
        return current.element;
    }
    public int height() { return height(root); }
    private int height(Node<E> node) {
        return node==null?-1:1+Math.max(height(node.left),height(node.right));
    }
    public List<E> preorder() {
        List<E> result=new ArrayList<>();preorder(root,result);return result;
    }
    private void preorder(Node<E> node,List<E> result) {
        if(node==null) return;
        result.add(node.element);preorder(node.left,result);preorder(node.right,result);
    }
    public List<E> inorder() {
        List<E> result=new ArrayList<>();inorder(root,result);return result;
    }
    private void inorder(Node<E> node,List<E> result) {
        if(node==null) return;
        inorder(node.left,result);result.add(node.element);inorder(node.right,result);
    }
    public List<E> postorder() {
        List<E> result=new ArrayList<>();postorder(root,result);return result;
    }
    private void postorder(Node<E> node,List<E> result) {
        if(node==null) return;
        postorder(node.left,result);postorder(node.right,result);result.add(node.element);
    }
    public List<E> levelOrder() {
        List<E> result=new ArrayList<>();
        if(root==null) return result;
        ArrayDeque<Node<E>> queue=new ArrayDeque<>();queue.add(root);
        while(!queue.isEmpty()) {
            Node<E> node=queue.remove();result.add(node.element);
            if(node.left!=null) queue.add(node.left);
            if(node.right!=null) queue.add(node.right);
        }
        return result;
    }
    public void clear() { root=null;size=0; }
    /** Package-private diagnostic: actual comparison-selected search path.
     * Returns visited elements through the match or the failed-search boundary. */
    List<E> searchPath(E element) {
        require(element);List<E> result=new ArrayList<>();Node<E> current=root;
        while(current!=null) {
            result.add(current.element);
            int comparison=element.compareTo(current.element);
            if(comparison==0) break;
            current=comparison<0?current.left:current.right;
        }
        return result;
    }
    /** Checks inherited bounds, identity uniqueness, and reachable count.
     * An identity set detects both cycles and multiple-parent child links. */
    boolean isValidBst() {
        Set<Node<E>> seen=Collections.newSetFromMap(new IdentityHashMap<>());
        int count=validate(root,null,null,seen);
        return count==size && ((root==null)==(size==0));
    }
    private int validate(Node<E> node,E lower,E upper,Set<Node<E>> seen) {
        if(node==null) return 0;
        if(!seen.add(node) || node.element==null) return -1;
        if(lower!=null && node.element.compareTo(lower)<=0) return -1;
        if(upper!=null && node.element.compareTo(upper)>=0) return -1;
        int left=validate(node.left,lower,node.element,seen);
        if(left<0) return -1;
        int right=validate(node.right,node.element,upper,seen);
        return right<0?-1:1+left+right;
    }
}
