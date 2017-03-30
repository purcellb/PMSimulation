/*=====================================================================================================
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
=====================================================================================================*/
package CSC318.EventBased;

import java.util.ArrayList;

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
        double numDocs = 1.0;    //how many docs are treating patients at the clinic
        double currentWait;
        int numEvent = 0;
        int totalHeart = 0, totalGastro = 0, totalBleed = 0,
                totalHeartDead = 0, totalGastroDead = 0, totalBleedDead = 0;

        //total wait time for each patient type (for avgs)


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
                case ARRIVAL: // arrival event------------------------------------------------------------------------

                    //new patient
                    Patient p = new Patient(patientID, current.getTime());

                    //add patient to line
                    patQ.addInOrder(p);

//                    //how long is the current waiting lines total treatment time? (sum of treatment times)
//                    currentWait = CalcCurrentWait(patQ);
//
//                    //how long is p's treatment time?
//                    double pTreatTime = TimeToTreat(p.getAilmentType(), numDocs);
//
//                    //set this patients treatment time to how long it will take to treat them
//                    p.settTreat(pTreatTime);
//
//                    //set this patients wait time to how long the current wait is counting their treatment time
//                    p.settWait(currentWait);



                    //gen new death event
                    eventTime = (bigTime + p.gettDeath());
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
                case DEATH: // death event------------------------------------------------------------------------
                    //patient died before treatment, remove from Qs
                    //resolve and track the dead patient
                    int died = RemovePatient(current.getPatient(), patQ);
                    switch (died) {
                        case 1:
                            totalHeartDead += 1;
                            break;
                        case 2:
                            totalGastroDead += 1;
                            break;
                        case 3:
                            totalBleedDead += 1;
                            break;
                        case -1:
                            System.err.printf("Tried to kill patient %d, didnt find them.\n", current.getPatient());
                    }
//                    //remove treatment event//DEPRECATED
//                    RemovePatientEvent(current.getPatient(), bigTime, EventType.TREATMENT, eventQ);

                    break;
                case TREATMENT: // treatment event------------------------------------------------------------------------
                    //treats next patient in line

                    //resolve and track the patient
                    Patient treating = (Patient)patQ.getValue(0);
                    //increment count of treated patients for type
                    switch (treating.getAilmentType()) {
                        case 1:
                            totalHeart += 1;
                            break;
                        case 2:
                            totalGastro += 1;
                            break;
                        case 3:
                            totalBleed += 1;
                            break;
                        case -1:
                            System.err.printf("Tried to treat patient %d, didnt find them.\n", treating.getID());
                    }
                    patQ.managedRemove(0);

                    //remove death event
                    RemovePatientEvent(treating.getID(), bigTime, EventType.DEATH, eventQ);

                    //TODO: Create next treatment event
                    //gen new treatmentevent
                    eventTime = (TimeToTreat());
                    Event te = new Event(eventTime, EventType.TREATMENT, patientID);
                    eventQ.addInOrder(te);

                    System.out.println("Patient " + patientID + " treatment event created at event time\t" + eventTime
                            + "\n\t\t\t\t\t\t\t from bigTime " + bigTime);

                    break;
                case END: // end simulation event-------------------------------------------------------------

                    //todo: Its reportin time
                    int totalTreated = totalBleed + totalGastro + totalHeart;
                    int totalDead = totalBleedDead + totalGastroDead + totalHeartDead;
                    int numpats = ((Patient) patQ.getValue(0)).getID() - 1;


                    System.out.printf("Total Patients = %d\n\n", numpats);

                    System.out.printf("Total Treated = %d  Bleed: %d    Gas: %d     Heart: %d   \n",
                            totalTreated, totalBleed, totalGastro, totalHeart);

                    System.out.printf("Total Dead = %d  Bleed: %d    Gas: %d     Heart: %d   \n",
                            totalDead, totalBleedDead, totalGastroDead, totalHeartDead);

                    System.out.println("Last Patient ID = " + patientID);
                    System.out.println("Total Events = " + numEvent);

                    System.exit(0);
            }

            //cycle to next event
            eventQ.managedRemove(0);
            current = (Event) eventQ.getValue(0);

        }//end of while(not event 4)

        //todo: Its reportin time
        int totalTreated = totalBleed + totalGastro + totalHeart;
        int totalDead = totalBleedDead + totalGastroDead + totalHeartDead;
        int numpats = ((Patient) patQ.getValue(0)).getID() - 1;


        System.out.printf("Total Patients = %d\n\n", numpats);

        System.out.printf("Total Treated = %d  Bleed: %d    Gas: %d     Heart: %d   \n",
                totalTreated, totalBleed, totalGastro, totalHeart);

        System.out.printf("Total Dead = %d  Bleed: %d    Gas: %d     Heart: %d   \n",
                totalDead, totalBleedDead, totalGastroDead, totalHeartDead);

        System.out.println("Last Patient ID = " + patientID);
        System.out.println("Total Events = " + numEvent);

        System.exit(0);

    }

    private static double CalcCurrentWait(GenericManager pQ) {
        double currentWait = 0.0;
        //get the sum of the treatment time for each person in line
        for (int i = 0; i < pQ.count; i++) {
            currentWait += ((Patient) (pQ.getValue(i))).gettTreat();
        }
        return currentWait;
    }


    private static int RemovePatient(int patient, GenericManager patQ) {
        Patient patientToRem;
        Patient tp;
        //removes death
        //search the ques for matching items and remove them
        for (int i = 0; i < patQ.getCount(); i++) {
            patientToRem = (Patient) patQ.getValue(i);
            if (patientToRem.getID() == patient) {

                patQ.managedRemove(i);
                patQ.sort();

                return patientToRem.getAilmentType(); //returns what patient ailment killed the patient
            }
        }
        return -1;
    }

    private static EventType RemovePatientEvent(int patient, double bigTime, EventType eventType, GenericManager eventQ) {

        Event e;
        Event e2;
        //search the ques for matching items and remove them
        for (int i = 0; i < eventQ.getCount(); i++) {
            e = (Event) eventQ.getValue(i);

            if (e.getPatient() == patient && e.getEventType() == eventType) {

//                //decrements the time for all other events for TREATMENTs that are removed infront of them
//                //since the treatment is removed, move other treatment events up
//                if (eventType == EventType.TREATMENT) {
//                    for (int j = 0; j < eventQ.getCount(); j++) {
//                        e2 = ((Event) eventQ.getValue(j));
//                        //event e is being removed from the Q, so update the other treatments
//                        if (e2.eventType == eventType.TREATMENT)
//                            //TODO: this needs to subtract only the dead patients treatment time, not the events time
//                        e2.setTime(e2.getTime() + (e.getTime())-bigTime);
//                    }
//                }

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
        if (bigX > 0.9) {
            bigX = Math.random();
        }
        deltaTime = -Math.log(1.0 - bigX) / .05;
        return deltaTime;

    }//end timetoarrive

    //generates new treatment duration for a patients treatment event
    public static double TimeToTreat(int a, double numDocs) {
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

    //arrival time, time waited, time till death, time in system
    protected double tArrive;
    protected double tTreat; //how long ilness will take to treat
    protected double tWait;
    protected double tDeath;
    protected int myDeath;
    protected int ID; //patient ID

    public Patient(int ID, double bigTime) {
        this.ID = ID;
        this.myDeath = ID;
        tWait = tTreat = 0;//defualt these to zero for safety
        this.ailmentType = setAilmentType();
        tArrive = bigTime;
        settDeath(); //generate patient death time

    }

    public double gettTreat() {
        return tTreat;
    }

    public void settTreat(double tTreat) {
        this.tTreat = tTreat;
    }

    public int getID() {
        return ID;
    }

    public int getMyDeath() {
        return myDeath;
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

    public void settArrive(double tArrive) {
        this.tArrive = tArrive;
    }

    public double gettWait() {
        return tWait;
    }

    public void settWait(double tWait) {
        this.tWait = tWait;
    }

    public double gettDeath() {
        return tDeath;
    }

    private void settDeath() {
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

        dTimer = sigma * (Math.random()) + mu;
        tDeath = tArrive + dTimer;
    }

    @Override

    public int compareTo(Object o) {
        if (gettArrive() > ((Patient) o).gettArrive()) {
            return 1;
        } else if (gettArrive() < ((Patient) o).gettArrive()) {
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
