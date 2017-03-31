/*=============================================================================
* @title Payless Medical Service
*   Event Based Simulation
* @author Bobby Purcell
* @description :
* Events:
*   1- PatientArrive, calls
*   2- PatientDeath
*   3- PatientTreatment, removes death event
*   4- SimulationEnd, calcs stats
*
==============================================================================*/
package CSC318.EventBased;

import java.util.ArrayList;
import java.util.Random;

//enumerated representation of events
enum EventType {
    ARRIVAL, DEATH, TREATMENT, END
}

@SuppressWarnings("unchecked")
public class EventBasedClinicSimulation {
    public static void main(String[] args) {

        double bigTime = 0.0; //the master clock
        double timeToRun = 6000; //6000 minutes = 100hrs
        double eventTime;// the event time

        GenericManager eventQ = new GenericManager<>(); //order of events
        GenericManager patQ = new GenericManager<>(); //patients in waiting room
        int patientID = 1;  //unique id for patients (and their death event when appropriate)
        double numDocs = 2.0;    //how many docs are treating patients at the clinic

        //
        int numEvent = 0;
        int totalHeart = 0, totalGastro = 0, totalBleed = 0,
                totalHeartDead = 0, totalGastroDead = 0, totalBleedDead = 0;

        //total wait time for each patient type (for avgs)
        double totalHeartWait = 0.0, totalGastroWait = 0.0, totalBleedWait = 0.0;

        //Makes new patient Patient(number,ailment)
        //adds the new patient to the arraylist of events in order
        //prime the Queue
        eventQ.addFront(new Event(0, EventType.ARRIVAL, patientID));
        eventQ.addEnd(new Event(timeToRun, EventType.END, -9999));
        Event current;

        while (bigTime <= timeToRun) {
            numEvent++;
            current = (Event) eventQ.getValue(0);

            bigTime = current.getTime();
            switch (current.eventType) {
                case ARRIVAL: // arrival event---------------------------------------------------

                    //new patient, generates ilness internally
                    Patient p = new Patient(patientID, current.getTime());

                    //if line is empty, get in the front of the line and spawn a treatment event
                    if (patQ.count == 0) {
                        patQ.addFront(p);

                        eventTime = 1 + bigTime;
                        Event te = new Event(eventTime, EventType.TREATMENT);
                        eventQ.addInOrder(te);
                    } else { //else add patient to line as normal
                        patQ.addInOrder(p);
                    }
                    //**NOTE** Heart patient priority in line is handled in the patient class's compareTo **NOTE**

                    //gen new death event
                    eventTime = ( p.gettDeath());
                    Event de = new Event(eventTime, EventType.DEATH, patientID);
                    eventQ.addInOrder(de);

                    patientID++;
                    //gen new arrival
                    try {
                        eventTime = (bigTime + TimeToArrive());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                    Event ae = new Event(eventTime, EventType.ARRIVAL, patientID);
                    eventQ.addInOrder(ae);
//                    System.out.println("Patient " + patientID + " arrival event created at event time\t" + eventTime
//                            + "\n\t\t\t\t\t\t\tfrom bigTime " + bigTime);

                    break;
                case DEATH: // death event----------------------------------------------------
                    //patient died before treatment, call the morgue, get the corpse outta here
                    //resolve and track the dead patient
                    int died = RemovePatient(current.getPatient(), patQ);
                    switch (died) {
                        case 1:
                            totalHeartDead += 1;
                            System.out.printf("\n*****HEART PATIENT # %d DIED IN LINE*****\n\n", current.getPatient());
                            break;
                        case 2:
                            totalGastroDead += 1;
                            System.out.printf("\n*****GASTRO PATIENT # %d DIED IN LINE*****\n\n", current.getPatient());
                            break;
                        case 3:
                            totalBleedDead += 1;
                            System.out.printf("\n*****BLEEDING PATIENT # %d DIED IN LINE*****\n\n", current.getPatient());
                            break;
                        case -1:
                            System.err.printf("Tried to kill patient %d, didnt find them.\n\n", current.getPatient());
                    }

                    break;
                case TREATMENT: // treatment event----------------------------------------
                    //treats current patient
                    //this event is essentially the doctor calling "NEXT!"
                    //if no ones in line, take a "break"
                    if (patQ.count <= 0) break;

                    //resolve and track the next patient in line
                    Patient treating = (Patient) patQ.getValue(0);

                    //increment count of treated patients for type
                    switch (treating.getAilmentType()) {
                        case 1:
                            totalHeart += 1;
                            totalHeartWait += treating.gettWait(bigTime);
                            break;
                        case 2:
                            totalGastro += 1;
                            totalGastroWait += treating.gettWait(bigTime);
                            break;
                        case 3:
                            totalBleed += 1;
                            totalBleedWait += treating.gettWait(bigTime);
                            break;
                        case -1:
                            System.err.printf("Tried to treat patient %d, didnt find them.\n", treating.getID());
                    }

                    //remove death event, patient cant die in treatment
                    RemovePatientEvent(treating.getID(), bigTime, EventType.DEATH, eventQ);

                    patQ.managedRemove(0);//the patient leaves the line and is now being treated

                    //gen new treatmentevent at current time + how long itll take to treat this patient

                    eventTime = (TimeToTreat(treating.getAilmentType(), numDocs)) + bigTime;
                    Event te = new Event(eventTime, EventType.TREATMENT);
                    eventQ.addInOrder(te);

                    System.out.println("Patient treatment event created\n at event time" + eventTime
                            + "\n from bigTime " + bigTime);

                    break;
                case END: // end simulation event-------------------------------------------------------------

            }

            //cycle to next event
            eventQ.managedRemove(0);
            current = (Event) eventQ.getValue(0);

        }//end of while(not event 4)

        int totalTreated = totalBleed + totalGastro + totalHeart;
        int totalDead = totalBleedDead + totalGastroDead + totalHeartDead;
        int numpats = patientID - patQ.count;

        System.out.println("Total Events = " + numEvent);
        System.out.printf("Total Patients in line = %d", patQ.count);
        System.out.println("\n\nNumber of Doctorss = " + numDocs);
        System.out.printf("Total Patients treated or dead = %d\n\n", numpats);

        System.out.printf("Total Treated = %d  Bleed: %d    Gas: %d     Heart: %d   \n",
                totalTreated, totalBleed, totalGastro, totalHeart);

        System.out.printf("Total Dead = %d  Bleed: %d    Gas: %d     Heart: %d   \n\n",
                totalDead, totalBleedDead, totalGastroDead, totalHeartDead);

        System.out.printf("Avg WaitTimes\n\tHeart: %f\tGas: %f\tBleed: %f\n",
                totalHeartWait / totalHeart, totalGastroWait / totalGastro, totalBleedWait / totalBleed);

        System.exit(0);

    }

    private static int RemovePatient(int patient, GenericManager patQ) {//todo verify this works
        Patient patientToRem;

        //removes death
        //search the ques for matching items and remove them
        for (int i = 0; i < patQ.getCount(); i++) {
            patientToRem = (Patient) patQ.getValue(i);
            if (patientToRem.getID() == patient) {
                patQ.managedRemove(i);
                return patientToRem.getAilmentType(); //returns what patient ailment killed the patient
            }
        }
        return -1;
    }

    private static EventType RemovePatientEvent(int patient, double bigTime, EventType eventType, GenericManager eventQ) {
        Event e;
        //search the ques for matching items and remove them
        for (int i = 0; i < eventQ.getCount(); i++) {
            e = (Event) eventQ.getValue(i);

            if (e.getPatient() == patient && e.getEventType() == eventType) {

                eventQ.managedRemove(i);
                return e.eventType; //returns what event was removed or null in fail case
            }
        }
        return null;
    }

    //generates new patient arrival from rate 3/hr
    public static double TimeToArrive() throws Exception {
        double deltaTime;
        double bigX;
        bigX = Math.random();

        deltaTime = 60 * Math.log(1.0 - bigX) / -3.0;
        return deltaTime;

    }//end timetoarrive

    //generates new treatment duration for a patients treatment event
    public static double TimeToTreat(int a, double numDocs) {
        double timeTreat;
        double bigx = Math.random();
//        while (bigx > 0.9 || bigx < 0.1) { //throw out extreme nums on the distro
//            bigx = Math.random();
//        }
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

        timeTreat = 60 * Math.log(1 - bigx) / (-rate * numDocs);
        return timeTreat;
    }//end timetoTreat


}//end EventBasedClinicSimulation

/*=====================================================================================================
Patient class
represents a patient at the clinic
Has Patient ID, Type of ailment, and arrival, wait, and total time
=====================================================================================================*/
class Patient implements Comparable {
    protected int ailmentType;  //1= heart,2=gastro, 3=bleeding

    //arrival time, time waited, time of death, time in system
    protected double tArrive;
    protected double tWait;
    protected double tDeath;
    protected int myDeath;
    protected int ID; //patient ID

    public Patient(int ID, double bigTime) {
        this.ID = ID;
        this.myDeath = ID;
        tWait = 0;//defualt  to zero for safety
        this.ailmentType = setAilmentType();
        tArrive = bigTime;
        settDeath(); //generate patient death time

    }

    public int getID() {
        return ID;
    }

    public int getAilmentType() {
        return ailmentType;
    }

    private int setAilmentType() {
        int r;

        int x = ((int) (Math.random() * 10));
        if (x <= 3) {
            r = 1; //Heart
        } else if (x <= 5) {
            r = 2;//Gastro
        } else r = 3;//Bleed
        return r;
    }

    public double gettArrive() {
        return tArrive;
    }

    public double gettWait(double bigTime) {
        return bigTime - tArrive;
    }

    public double gettDeath() {
        return tDeath;
    }

    private void settDeath() {
        double dTimer;
        Random r = new Random();
        int t = getAilmentType();
        double mu = 0.0, sigma = 0.0;
        switch (t) {

            case 1:
                sigma = 10;
                mu = 35;
                break;
            case 2:
                sigma = 30;
                mu = 80;
                break;
            case 3:
                sigma = 20;
                mu = 65;
                break;
            default:
                System.err.println("This patient doesnt have an illness and expects to die!");
                System.exit(1);//there is a serious problem, exit
        }

        dTimer = sigma * r.nextGaussian() + mu;
        tDeath = tArrive + dTimer;
    }

    @Override
    public int compareTo(Object o) {
        //it occurs to me that it makes real life logical sense to handle heart patients in the patient comparator
        int la = getAilmentType();//list patient ailment
        int ca = ((Patient) o).getAilmentType();//compared ailment
        boolean bothHeart = (la == 1 && ca == 1);   //both are heart patients
        boolean neitherHeart = (la != 1 && ca != 1); //neither are heart patients

        if (bothHeart || neitherHeart) { //if theres no heart patients involved or both are compare as normal
            if (gettArrive() > ((Patient) o).gettArrive()) {
                return 1;
            } else if (gettArrive() < ((Patient) o).gettArrive()) {
                return -1;
            } else {
                return 0;
            }
        } else if (la == 1) { //the heart patient is the one in line already
            return -1;
        } else          //the heart patient is the one joining the line and should be ahead of this non heart patient
            return 1;
    }
}//End Patient Class


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
        count++;
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
represents event type, when the event occurs, and which patient it belongs to
modified from Kent Pickett's code
=====================================================================================================*/
class Event implements Comparable {

    protected EventType eventType; // event type
    protected int patient; // which patient this event belongs to
    private double time; //when this even occurs

    public Event(double time, EventType eventType, int patient) {
        this.eventType = eventType;
        this.time = time;
        this.patient = patient;
    }

    //overloaded constructor for treatment events, theyre not associated with specific patients
    public Event(double time, EventType eventType) {
        this.eventType = eventType;
        this.time = time;
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

    public int getPatient() {
        return patient;
    }

    public EventType getEventType() {
        return eventType;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}//end of Event
