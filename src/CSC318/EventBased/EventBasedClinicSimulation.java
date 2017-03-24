/*=====================================================================================================
* @title Payless Medical Service
*   Event Based Simulation
* @author Bobby Purcell
* @description : not exactly my magnum opus but it works well enough
* Events:
*   1- PatientArrive, calls
*   2- PatientDeath
*   3- PatientTreatment, removes death event
*   4- SimulationEnd, calcs stats
*
=====================================================================================================*/
package CSC318.EventBased;

import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class EventBasedClinicSimulation {

    public static void main(String[] args) {

        double bigTime = 0.0; //the master clock
        double timeToRun = 6000; //6000 minutes = 100hrs
        double EventTime;// the event time
        double deltime; //change in time
        //TODO: setup patient death time tracking something something

        GenericManager eventQ = new GenericManager<Event>(); //order of events
        GenericManager patQ = new GenericManager<Patient>(); //patients in waiting room
        int patientID = 0;  //unique id for patients (and their death event when appropriate)
        double numDocs = 1.0;    //how many docs are treating patients at the clinic
        int numWaiting;
        int numEvent;
        int totalHeart = 0, totalGastro = 0, totalBleed = 0,
                totalHeartDead = 0, totalGastroDead = 0, totalBleedDead = 0;

        //total wait time for each patient type (for avgs)
        double totalTimeWaitHeart = 0.0, totalTimeWaitGastro = 0.0, totalTimeWaitBleed = 0.0;
        //,totalTimeWaitHeart2 = 0.0, totalTimeWaitGastro2 = 0.0, totalTimeWaitBleed2 = 0.0;
        int myDeadPatient;
        boolean doctorIsBusy, doctorIsBusy2;


        //Makes new patient Patient(number,ailment)
        //adds the new patient to the arraylist of events in order
        //prime the Queue
        patientID++;
        eventQ.addFront(new Event(0, 1, patientID));
        eventQ.addEnd(new Event(timeToRun, 4, -9999));
        Event current = (Event) eventQ.getValue(0);
        while (current.getEventType() != 4) {
            deltime = current.getTime() - bigTime;


            //cycle to next event
            eventQ.managedRemove(0);
            current = (Event) eventQ.getValue(0);

        }//end of while(not event 4)
        //todo: Its reportin time
    }

    public static void AddEvent() {

    }

    //generates new patient arrival from rate 3/hr
    public static double TimeToArrive() {
        double deltime;
        double bigX;
        double bigx = Math.random();
        if (bigx > .9) bigx = Math.random();
        deltime = -Math.log(1.0 - bigx) / 3.0;
        return deltime;
    }//end timetoarrive

    //generates new patient arrival from rate 3/hr
    public static double TimeToTreat(int a, int numDocs) {
        double timeTreat;
        double bigx = Math.random();
        double rate = 0.0; //number of patients/hr

        switch (a) {

            case 1://Heart
                rate = 2.0;
                break;
            case 2://Gastro
                rate = 4.0;
                break;
            case 3://Bleed
                rate = 6.0;
                break;
            default:
                System.err.println("Wtf? This patient doesnt have an illness! Literally impossible.");
                System.exit(1);//there is a serious problem, exit
        }
        timeTreat = 60 * Math.log(1.0 - bigx) / -(rate * numDocs);
        return timeTreat;
    }//end timetoarrive


}//end EventBasedClinicSimulation

/*=====================================================================================================
Patient class
represents a patient at the clinic
Has Patient ID, Type of ailment, and arrival, wait, and total time
=====================================================================================================*/
class Patient implements Comparable {
    protected int ailmentType;  //1= heart,2=gastro, 3=bleeding
    //arrival time, time waited, time till death, time in system
    protected double tArrive;
    protected double tWait;
    protected double tDeath;
    protected int myDeath;
    protected int ID; //patient ID

    public Patient(int ID) {
        this.ID = ID;
        this.myDeath = ID;
        this.ailmentType = setAilmentType();
        settDeath(); //generate patient death time
        tArrive = tWait = 0;
    }

    public int getAilmentType() {
        return ailmentType;
    }

    private int setAilmentType() {
        int r;

        int x = ((int) (Math.random() * 100));
        if (x < 30) {
            r = 1; //Heart
        } else if (x < 50) {
            r = 2;//Gastro
        } else r = 3;//Bleed
        return r;
    }


    public double gettArrive() {
        return tArrive;
    }

    public void settArrive(double tArrive) {
        this.tArrive = tArrive;
    }

    public double gettWait() {
        return tWait;
    }

    public void settWait(double tWait) {
        this.tWait += tWait;
    }

    public double gettDeath() {
        return tDeath;
    }

    public void settDeath() {
        double dTimer;
        int t = getAilmentType();
        double mu = 0.0, sigma = 0.0;
        switch (t) {

            case 1:
                mu = 10;
                sigma = 35;
                break;
            case 2:
                mu = 30;
                sigma = 80;
                break;
            case 3:
                mu = 20;
                sigma = 65;
                break;
            default:
                System.err.println("Wtf? This patient doesnt have an illness and expects to die!");
                System.exit(1);//there is a serious problem, exit
        }

        dTimer = sigma * (Math.random()) + mu;      //TODO: not random enough yet
        tDeath = dTimer;
    }

    @Override
    public int compareTo(Object o) {
        if (gettWait() > ((Patient) o).gettWait()) {
            return 1;
        } else if (gettWait() < ((Patient) o).gettWait()) {
            return -1;
        } else {
            return 0;
        }

    }
}

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
        if (eventType == 2)
            this.customer = customer;
        else
            this.customer = -9;
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
