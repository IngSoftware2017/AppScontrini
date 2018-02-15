package com.sw.ing.gestionescontrini.iterator;

/**
 * Define the the single node of a LinkedList that will contain the bitmap of the TicketEntity and the
 * most important info extract from the xml file.
 *
 * Created by Matteo Mascotto on 18/11/2017.
 */

public class Node<Bundle> {

    public Node<Bundle> next;
    public Bundle Element;

    public Node(Bundle Element, Node next) {
        this.Element = Element;
        this.next = next;
    }

    public Bundle getElement() {
        return this.Element;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }
}
