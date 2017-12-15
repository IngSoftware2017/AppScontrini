package Iterator;

import android.support.annotation.NonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Linkedlist iterator of bitmap for simplify the code testing of OCR module
 *
 * Created by matteo.mascotto on 18/11/2017.
 */

public class TicketsIterator<Bitmap> implements Iterable<Bitmap> {
    private Node<Bitmap> head, tail;

    /**
     * Add a new Ticket at the LinkedList
     *
     * @param element the bitmap image we want to insert in the LinkedList
     */
    public void add(Bitmap element) {
        Node<Bitmap> newNode = new Node<Bitmap>(element, null);

        // Check if the TicketsIterator is empty so the element that we will insert it's the only
        // one in the TicketsIterator, otherwise it will insert at the end of the LinkedList
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            // tail.setNext(newNode);
            tail = newNode;
        }
    }

    /**
     * Return the Bitmap element of a specific Node
     *
     * @param element the bitmap image we want to obtain from the LinkedList
     */
    public Bitmap get(Bitmap element) {
        if (head.getElement().equals(element)) {
            return (Bitmap) head.getElement();
        } else {
            Node current = head.getNext();

            while (current.getNext() != null) {
                if (current.getElement().equals(element)) { return (Bitmap) current.getElement(); }

                current.getNext();
            }

            if (current.getElement().equals(element)) { return (Bitmap) current.getElement(); }
        }

        return null;
    }

    /**
     * Remove the element from the LinkedList
     *
     * @param element the element to remove
     */
    public void remove(Bitmap element) {
        if (head.getElement().equals(element)) {
            head = head.getNext();
        } else {
            Node<Bitmap> current = head;
            Node<Bitmap> next = head.getNext();

            while (next != null) {
                if (next.getElement().equals(element)) {
                    current.setNext(next.getNext());

                    if (next.getNext() == null) { tail = current; }

                    return;
                }

                current = next;
                next = next.getNext();
            }
        }
    }

    /*
     * Interface of the iterator of the class
     */
    @NonNull
    @Override
    public Iterator<Bitmap> iterator() {
        return new LinkedListIterator();
    }

    /*
     * Operations to do for each element of the LinkedList - actual empty (18/11/2017)
     */
    @Override
    public void forEach(Consumer<? super Bitmap> action) {

    }

    /*
     * Iterator of Bitmap for the iterable class TicketsIterator
     *
     * Created by matteo.mascotto on 18/11/2017.
     */
    private class LinkedListIterator implements Iterator<Bitmap> {

        Node<Bitmap> current = null;

        /*
         * Verify if there's one more element into the LinkedList
         */
        @Override
        public boolean hasNext() {
            if (current == null && head != null) {
                return true;
            } else if (current != null) {
                return current.getNext() != null;
            }
            return false;
        }

        /*
         * It return the next bitmap element in the LinkedList
         */
        @Override
        public Bitmap next() {
            if (current == null && head != null) {
                current = head;
                return head.getElement();
            } else if (current != null) {
                current = current.getNext();
                return current.getElement();
            }

            throw new NoSuchElementException();
        }
    }
}
