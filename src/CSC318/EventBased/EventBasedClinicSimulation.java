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
    static double totalTimeWaitHeart = 0.0, totalTimeWaitGastro = 0.0, totalTimeWaitBleed = 0.0;

    public static void main(String[] args) {

        double bigTime = 0.0; //the master clock
        double timeToRun = 6000; //6000 minutes = 100hrs
        double eventTime;// the event time
        double deltime; //change in time
        //TODO: setup patient death time tracking something something

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
        eventQ.addFront(new Event(0, 1, patientID));
        eventQ.addEnd(new Event(timeToRun, 4, -9999));
        Event current = (Event) eventQ.getValue(0);

        while (current.getEventType() != 4 && bigTime <=timeToRun) {
            numEvent++;
            deltime = current.getTime() - bigTime;
            eventTime = bigTime + deltime;
            bigTime = current.getTime();
            switch (current.eventType) {
                case 1: // arrival event------------------------------------------------------------------------

                    //new patient
                    Patient p = new Patient(patientID, bigTime);

                    //add patient to line
                    patQ.addInOrder(p);

                    //how long is the current waiting lines total treatment time? (sum of treatment times)
                    currentWait = CalcCurrentWait(patQ);

                    //how long is p's treatment time?
                    double pTreatTime = TimeToTreat(p.getAilmentType(), numDocs);

                    //set this patients treatment time to the wait + their time to treat
                    p.settTreat(pTreatTime);
                    p.settWait(currentWait);
                    //i set this AFTER the sum of current treatment time so the patients
                    //own treatment time doesnt factor into their wait to be treated

                    //gen new treatmentevent
                    // (current big time, plus ps treatment time, plus the people ahead of p in line)1
                    eventTime = (p.gettWait());
                    Event te = new Event(eventTime, 3, patientID);
                    eventQ.addInOrder(te);

                    //gen new death event
                    eventTime = (bigTime + p.gettDeath()); //patient.
                    Event de = new Event(eventTime, 2, patientID);
                    eventQ.addInOrder(de);

                    patientID++;
                    //gen new arrival
                    eventTime = (bigTime + TimeToArrive());
                    Event ae = new Event(eventTime, 1, patientID);
                    eventQ.addInOrder(ae);


                    break;
                case 2: // death event------------------------------------------------------------------------
                    //patient died before treatment, remove from Qs
                    //resolve and track the dead patient
                    int died = KillPatient(current.getPatient(), patQ);
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
                        default:
                            System.err.printf("Tried to kill patient %d, didnt find them.\n", current.getPatient());
                    }
                    //remove treatment event
                    KillPatientEvent(current.getPatient(), 3, eventQ);

                    break;
                case 3: // treatment event------------------------------------------------------------------------
                    //patient treated before death
                    //resolve and track the patient
                    int treated = TreatPatient(current.getPatient(), patQ);
                    switch (treated) {
                        case 1:
                            totalHeart += 1;
                            break;
                        case 2:
                            totalGastro += 1;
                            break;
                        case 3:
                            totalBleed += 1;
                            break;
                        default:
                            System.err.printf("Tried to treat patient %d, didnt find them.\n", current.getPatient());
                    }
                    //remove death event
                    KillPatientEvent(current.getPatient(), 2, eventQ);

                    break;
                case 4: // end simulation event-------------------------------------------------------------
                    //go home end sim event. youre drunk.
                    System.err.println("should not be here and if i am i've got event proc issues");
                    System.exit(1);
                    break;
            }

            //cycle to next event
            eventQ.managedRemove(0);
            current = (Event) eventQ.getValue(0);
            //patQ.sort();  //TODO: do i need to sort here or not? i cant even tell whats wrong when i sort vs not sort
            //eventQ.sort();
        }//end of while(not event 4)
        //todo: Its reportin time
        int totalTreated = totalBleed + totalGastro + totalHeart;
        int totalDead = totalBleedDead + totalGastroDead + totalHeartDead;

        System.out.println("Total Treated =" + totalTreated);
        System.out.println("Total Dead =" + totalDead);
    }

    private static double CalcCurrentWait(GenericManager pQ) {
        double currentWait = 0.0;
        //get the sum of the treatment time for each person in line
        for (int i = 0; i < pQ.count; i++) {
            currentWait += ((Patient) (pQ.getValue(i))).gettTreat();
        }
        return currentWait;
    }

    private static int KillPatient(int patient, GenericManager patQ) {
        boolean removedp = false;
        Patient p = null;
        //removes treatment
        //search the ques for matching items and remove them
        for (int i = 0; i < patQ.getCount(); i++) {
            p = (Patient) patQ.getValue(i);
            if (p.getID() == patient) {
                patQ.managedRemove(i);
                removedp = true;
            }
        }

        if (removedp) return p.getAilmentType(); //returns what patient ailment caused death
        else return -1;
    }

    private static int TreatPatient(int patient, GenericManager patQ) {
        boolean removedp = false;
        Patient p = null;
        //removes death
        //search the ques for matching items and remove them
        for (int i = 0; i < patQ.getCount(); i++) {
            p = (Patient) patQ.getValue(i);
            if (p.getID() == patient) {
                patQ.managedRemove(i);
                removedp = true;
            }
        }

        if (removedp) return p.getAilmentType(); //returns what patient ailment was treated
        else return -1;
    }

    private static int KillPatientEvent(int patient, int eventType, GenericManager eventQ) {
        boolean removede = false;
        Event e = null;
        //search the ques for matching items and remove them
        for (int i = 0; i < eventQ.getCount(); i++) {
            e = (Event) eventQ.getValue(i);
            if (e.getPatient() == patient && e.getEventType() == eventType) {
                eventQ.managedRemove(i);
                removede = true;
            }
        }
        if (removede) return e.eventType; //returns what event was removed
        else return -1;
    }


    //generates new patient arrival from rate 3/hr
    public static double TimeToArrive() { //todo: pretty sure this implementation is the core of my issues
        double deltime;
        double bigx = Math.random();
        //while (bigx > .9) bigx = Math.random();
        deltime = 60 * (Math.log(bigx)/-3.0);
        return deltime;
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

        timeTreat = 60 * Math.log( bigx) / -(rate * numDocs);
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
    protected double tTreat;
    protected double tWait;
    protected double tDeath;
    protected int myDeath;
    protected int ID; //patient ID

    public Patient(int ID, double bigTime) {
        this.ID = ID;
        this.myDeath = ID;
        tWait = tTreat = 0;//defualt these to zero for safety
        this.ailmentType = setAilmentType();
        settDeath(); //generate patient death time
        tArrive = bigTime;
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

    protected int eventType; // event type
    protected int patient; // which patient this event belongs to
    private double time; //when this even occurs

    public Event(double time, int eventType, int patient) {
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

    public int getEventType() {
        return eventType;
    }

    public double getTime() {
        return time;
    }
}//end of Event
