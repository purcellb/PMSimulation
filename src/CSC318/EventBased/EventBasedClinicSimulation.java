/*=====================================================================================================
* @title Payless Medical Service
*   Event Based Simulation
* @author Bobby Purcell
* @description /TODO: Write Desc
=====================================================================================================*/
package CSC318.EventBased;

import java.util.ArrayList;

public class EventBasedClinicSimulation {

    public static void main(String[] args) {

        //TODO: arrival rate of 3/hr poisson distribution
        //TODO: NewPatient event
        //Calls UniformlyDistributedAilment to get what kind of patient to make
        //Makes new patient Patient(number,ailment)

        //TODO: doctor treatment rates, probably dont need doctor objects

    }
}
//TODO: Patient class
//TODO: Patient ID, Type of ailment, Patient balk/death time(this is normally distributed SD's are listed),
//constructor determines death time
//adds the new patient to the arraylist of events in order


/*=====================================================================================================
GenericManager class
represents an array list of objects that can be compared to each other
objects can be added first, last, or added in order by comparator
mostly a copy of Kent Pickett's code - If it ain't broke don't fix it.
=====================================================================================================*/
class GenericManager<T extends Comparable<? super T>> {

    protected ArrayList<T> list = new ArrayList<>();
    protected int count; //items in the arraylist

    //generic constructor
    public GenericManager() {
        //initialize to 0
        this.count = 0;
    }

    public int getCount() {
        return count;
    }

    public int addFront(T x) {
        list.add(0, x);
        count++;
        return count;
    }

    public int addEnd(T x) {
        list.add(count++, x);
        return count;
    }

    //adds object x to the list a the position determined by its comparator
    public int addInOrder(T x) {
        int i;
        if ((count == 0)
                || ((x.compareTo(list.get(0)) == -1)
                || x.compareTo(list.get(0)) == 0)) {
            //object goes at the front of the list
            list.add(0, x);
        } else if ((x.compareTo(list.get(count - 1)) == 1)
                || (x.compareTo(list.get(count - 1)) == 0)) {
            //object goes at the end of the list
            list.add(count, x);
        } else {
            //object goes somewhere in the middle of the list
            i = 0;
            //compare x with the list from start until x > the current item
            while ((i < count) && (x.compareTo(list.get(i)) == 1)) i++;
            //add x in its place after the current item
            list.add(i, x);
        }
        return count;
    }

    public T getValue(int i) {
        if (i < count) {
            return list.get(i);
        } else {
            System.err.println(String.format("Attempted to get value from a position that doesn't exist: %d", i));
            return list.get(0);//default case
        }
    }

    //basic generic sorting method, uses object's compareTo
    public void sort() {
        T xsave, ysave, a, b;
        int isw = 1; //is the list sorting
        int xlast = list.size();
        while (isw == 1) {
            isw = 0;
            for (int i = 0; i <= xlast - 2; i++) {
                a = list.get(i);
                b = list.get(i + 1);
                switch (a.compareTo(b)) {
                    case 1://already sorted, break
                        break;
                    case -1://objects out of order, sort
                        xsave = list.get(i);
                        ysave = list.get(i + 1);
                        list.remove(i);
                        list.add(i, ysave);
                        list.remove(i + 1);
                        list.add(i + 1, xsave);
                        isw = 1;
                        break;
                    default://objects assumed to be equal
                }
            }
        }
    }//end of sorting method

    //removes item at specified index and decrements the count
    public void managedRemove(int i) {
        if ((i >= 0) && (i <= count - 1)) {
            list.remove(i);
            count--;
        }
    }

}//end of generic mgr

/*=====================================================================================================
Event class
represents event type, when the event occurs, and which customer it belongs to
modified from Kent Pickett's code
=====================================================================================================*/
class Event implements Comparable {

    protected int eventType; // event type
    protected int customer; // which customer this event belongs to
    private double time; //when this even occurs

    public Event(double time, int eventType, int customer) {
        this.eventType = eventType;
        this.time = time;
        this.customer = customer;
    }

    @Override
    //compares based on the events' times
    public int compareTo(Object o) {
        if (getTime() > ((Event) o).getTime()) {
            return 1;
        } else if (getTime() < ((Event) o).getTime()) {
            return -1;
        } else {
            return 0;
        }

    }

    public int getCustomer() {
        return customer;
    }

    public int getEventType() {
        return eventType;
    }

    public double getTime() {
        return time;
    }
}//end of Event
