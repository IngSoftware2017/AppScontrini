package com.sw.ing.iteratorlibrary.iterator;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Linkedlist iterator of bitmap for simplify the code testing of OCR module
 *
 * Created by matteo.mascotto on 18/11/2017.
 */

public class TicketsIterator<Type> implements Iterable<Type> {
    private Node<Type> head, tail;

    public TicketsIterator(){
    }
    /**Created by matteo.mascotto
     * Add a new Ticket at the LinkedList
     *
     * @param element the bitmap image we want to insert in the LinkedList
     */
    public void add(Type element) {
        Node<Type> newNode = new Node<Type>(element, null);
        // Check if the TicketsIterator is empty so the element that we will insert it's the only
        // one in the TicketsIterator, otherwise it will insert at the end of the LinkedList
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            tail = newNode;
        }
    }

    /**Created by matteo.mascotto
     * Return the Type element of a specific Node
     *
     * @param element the bitmap image we want to obtain from the LinkedList
     */
    public Type get(Type element) {
        if (head.getElement().equals(element)) {
            return (Type) head.getElement();
        } else {
            Node current = head.getNext();
            while (current.getNext() != null) {
                if (current.getElement().equals(element))
                    return (Type) current.getElement();
                current.getNext();
            }
            if (current.getElement().equals(element)) { return (Type) current.getElement(); }
        }
        return null;
    }

    /**Created by matteo.mascotto
     * Remove the element from the LinkedList
     *
     * @param element the element to remove
     */
    public void remove(Type element) {
        if (head.getElement().equals(element)) {
            head = head.getNext();
        } else {
            Node<Type> current = head;
            Node<Type> next = head.getNext();

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

    /*Created by matteo.mascotto
     * Interface of the iterator of the class
     */
    @NonNull
    @Override
    public Iterator<Type> iterator() {
        return new LinkedListIterator();
    }

    /*Created by matteo.mascotto
     * Operations to do for each element of the LinkedList - actual empty (18/11/2017)
     */
    @Override
    public void forEach(Consumer<? super Type> action) {

    }

    /** Created by Federico taschin
     * @return the number of nodes in the iterator
     */
    public int size(){
        int cont = 0;
        Node<Type> pointer = head;
        while(pointer!=null){
            cont++;
            pointer = pointer.getNext();
        }
        return cont;
    }

    /** Created by Federico Taschin
     *  This class is the iterator builder.
     */
    public static class IteratorBuilder{
        private String xmlName;

        /**Created by Federico Taschin
         * sets the XML file on which the iterator is based
         * @param xmlName not null, only the name (not the path) of the xml file WITH EXTENSION
         * @return istance of IteratorBuilder
         */
        public IteratorBuilder setXML(String xmlName){
            this.xmlName = xmlName;
            return this;
        }

        /** Created by Federico Taschin
         * Builds the iterator by loading the xml file and parsing it
         * @return Iterator<Bundle> not null
         * @throws ParserConfigurationException
         * @throws SAXException
         * @throws IOException if the xml file is unreachable
         */
        public Iterator<Bundle> build() throws ParserConfigurationException, SAXException, IOException {
            TicketsIterator ticketsIterator = new TicketsIterator();
            XMLParser parser = new XMLParser(xmlName);
            parser.parseXML();
            ArrayList<TicketInfo> tickets = parser.getTicketInfos();
            for(TicketInfo info : tickets){
                Bundle bundle = new Bundle();
                bundle.setTicketInfo(info);
                ticketsIterator.add(bundle);
            }
            return ticketsIterator.iterator();
        }
    }

    /*
     * Iterator of Type for the iterable class TicketsIterator
     * Created by matteo.mascotto on 18/11/2017.
     */
    private class LinkedListIterator implements Iterator<Type> {
        Node<Type> current = null;

        /*Created by matteo.mascotto
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

        /*Created by matteo.mascotto
         * It return the next bitmap element in the LinkedList
         */
        @Override
        public Type next() {
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
