package com.sw.ing.gestionescontrini.iterator;

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
 * Created by Matteo Mascotto on 18/11/2017.
 */

public class TicketsIterator<Type> implements Iterable<Type> {
    private Node<Type> head, tail;

    /**
     * Created by Federico Taschin
     *
     */
    public TicketsIterator(){

    }
    /**
     * Add a new TicketEntity at the LinkedList
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

    /**
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
                if (current.getElement().equals(element)) { return (Type) current.getElement(); }

                current.getNext();
            }

            if (current.getElement().equals(element)) { return (Type) current.getElement(); }
        }

        return null;
    }

    /**
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

    /*
     * Interface of the iterator of the class
     */
    @NonNull
    @Override
    public Iterator<Type> iterator() {
        return new LinkedListIterator();
    }

    /*
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

    public static class IteratorBuilder{
        private String xmlName;

        public IteratorBuilder setXML(String xmlName){
            this.xmlName = xmlName;
            return this;
        }
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

            Iterator<Bundle> iterator = ticketsIterator.iterator();


            while(iterator.hasNext()){
                Bundle b = iterator.next();
                Bitmap bitmap = b.getBitmap();
                Log.d("ABCDEF","ticket id:"+b.getTicketInfo().getID()+" dim:"+b.getBitmap().getWidth()+","+b.getBitmap().getHeight());
            }
            return ticketsIterator.iterator();
        }
    }

    /*
     * Iterator of Type for the iterable class TicketsIterator
     *
     * Created by Matteo Mascotto on 18/11/2017.
     */
    private class LinkedListIterator implements Iterator<Type> {

        Node<Type> current = null;

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
