package Iterator;

import android.graphics.Bitmap;

/**
 * Define the the single node of a LinkedList that will contain the bitmap of the Ticket and the
 * most important info extract from the xml file.
 *
 * Created by matteo.mascotto on 18/11/2017.
 */

public class Node<Bitmap> {

    public Node<Bitmap> next;
    public Bitmap Element;

    public Node(Bitmap Element, Node next) {
        this.Element = Element;
        this.next = next;
    }

    public Bitmap getElement() {
        return this.Element;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {

    }
}
