package com.kishuu.browserhistory.datastructure;

import java.util.EmptyStackException;

/**
 * A minimal LIFO stack built from scratch (singly linked, not java.util.Stack)
 * so it's explicit what data structure is doing the work. Used to implement
 * back/forward navigation the same way a real browser does:
 *
 *   - Visiting a new page  -> push onto backStack, clear forwardStack
 *   - Clicking Back        -> pop backStack, push it onto forwardStack, show new backStack top
 *   - Clicking Forward     -> pop forwardStack, push it onto backStack, show it
 *
 * Generic so it can hold HistoryNode references directly.
 */
public class NavigationStack<T> {

    private static class Node<T> {
        T value;
        Node<T> below;
        Node(T value, Node<T> below) {
            this.value = value;
            this.below = below;
        }
    }

    private Node<T> top;
    private int size;

    public void push(T value) {
        top = new Node<>(value, top);
        size++;
    }

    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        T value = top.value;
        top = top.below;
        size--;
        return value;
    }

    public T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return top.value;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int size() {
        return size;
    }

    public void clear() {
        top = null;
        size = 0;
    }
}
