// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

import java.util.Random;

// Car class
public class Car implements Runnable {
    private static Random rand = new Random(System.currentTimeMillis());
    private Roundabout roundabout;
    private int turns; // Number of [remaining] sections to go
    private int entry; // Entry section number

    public Car(Roundabout roundabout) {
        this.roundabout = roundabout;
        this.entry = Car.rand.nextInt(4);
        this.turns = Car.rand.nextInt(4) + 1;
    }

    @Override
    public void run() {
        try {
            String name = "Car#" + Thread.currentThread().getId();
            int currentSection = entry;
            int nextSection = (entry + 1) % 4;
            int previousSection = (entry + 3) % 4;

            roundabout.log(name + " is waiting at entry #" + entry);
            roundabout.getLock(previousSection).lockInterruptibly();

            try {
                // While the previous section is busy, wait
                while (!roundabout.isSectionFree(previousSection))
                    roundabout.getCondition(previousSection).await();
            } finally {
                roundabout.getLock(previousSection).unlock();
            }

            roundabout.getLock(currentSection).lockInterruptibly();

            try {
                // Enter the section
                roundabout.enterSection(currentSection);
            } finally {
                roundabout.getLock(currentSection).unlock();
            }

            roundabout.log(name + " entered in section #" + currentSection
                    + "; exits at section #" + (entry + turns) % 4);

            while (turns > 0) {
                Thread.sleep(1000); // The car takes a second to travel along a section
                roundabout.getLock(currentSection).lockInterruptibly();

                try {
                    // Leave the current section
                    roundabout.leaveSection(currentSection);

                    if (roundabout.isSectionFree(currentSection))
                        roundabout.getCondition(currentSection).signalAll();
                } finally {
                    roundabout.getLock(currentSection).unlock();
                }

                roundabout.getLock(nextSection).lockInterruptibly();

                try {
                    // Enter the next section
                    roundabout.enterSection(nextSection);
                } finally {
                    roundabout.getLock(nextSection).unlock();
                }

                roundabout.log(name + " moved to section #" + nextSection
                        + " (" + --turns + " turn(s) left)");
                currentSection = (currentSection + 1) % 4;
                nextSection = (nextSection + 1) % 4;
            }

            roundabout.getLock(currentSection).lockInterruptibly();

            try {
                // When all the sections had been covered, leave the roundabout
                roundabout.leaveSection(currentSection);

                if (roundabout.isSectionFree(currentSection))
                    roundabout.getCondition(currentSection).signalAll();
            } finally {
                roundabout.getLock(currentSection).unlock();
            }

            roundabout.log(name + " exited at section #" + currentSection);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


//
//             Alternative version (with nested locks)
//             =======================================
//
//
//import java.util.Random;
//import java.util.concurrent.locks.ReentrantLock;
//
//// Car class
//public class Car implements Runnable {
//    private static Random rand = new Random(System.currentTimeMillis());
//    private ReentrantLock firstLock, secondLock;
//    private Roundabout roundabout;
//    private int turns; // Number of [remaining] sections to go
//    private int entry; // Entry section number
//
//    public Car(Roundabout roundabout) {
//        this.roundabout = roundabout;
//        this.entry = Car.rand.nextInt(4);
//        this.turns = Car.rand.nextInt(4) + 1;
//    }
//
//    // Assign a priority to the locks (lower section number => higher priority);
//    // solves circular deadlock problem.
//    private void setLocksPriority(int fstSection, int sndSection) {
//        if (fstSection < sndSection) {
//            firstLock = roundabout.getLock(fstSection);
//            secondLock = roundabout.getLock(sndSection);
//        } else {
//            firstLock = roundabout.getLock(sndSection);
//            secondLock = roundabout.getLock(fstSection);
//        }
//    }
//
//    @Override
//    public void run() {
//        try {
//            String name = "Car#" + Thread.currentThread().getId();
//            int currentSection = entry;
//            int nextSection = (entry + 1) % 4;
//            int previousSection = (entry + 3) % 4;
//            boolean passed = false;
//
//            roundabout.log(name + " is waiting at entry #" + entry);
//            setLocksPriority(previousSection, currentSection);
//
//            while (!passed) {
//                firstLock.lockInterruptibly();
//
//                try {
//                    secondLock.lockInterruptibly();
//
//                    try {
//                        // If the previous section is free, enter the entry section
//                        if (roundabout.isSectionFree(previousSection)) {
//                            roundabout.enterSection(currentSection);
//                            roundabout.log(name + " entered in section #" + currentSection
//                                    + "; exits at section #" + (entry + turns) % 4);
//                            passed = true; // The car has entered the roundabout
//                        }
//                    } finally {
//                        secondLock.unlock();
//                    }
//                } finally {
//                    firstLock.unlock();
//                }
//
//                // If the car hasn't passed, wait 300ms
//                if (!passed)
//                    Thread.sleep(300);
//            }
//
//            while (turns > 0) {
//                Thread.sleep(1000); // The car takes a second to travel along a section
//                setLocksPriority(currentSection, nextSection);
//                firstLock.lockInterruptibly();
//
//                try {
//                    secondLock.lockInterruptibly();
//
//                    try {
//                        // Leave the current section and enter the next one
//                        roundabout.leaveSection(currentSection);
//                        roundabout.enterSection(nextSection);
//                        roundabout.log(name + " moved to section #" + nextSection
//                                + " (" + --turns + " turn(s) left)");
//                    } finally {
//                        secondLock.unlock();
//                    }
//                } finally {
//                    firstLock.unlock();
//                }
//
//                currentSection = (currentSection + 1) % 4;
//                nextSection = (nextSection + 1) % 4;
//            }
//
//            roundabout.getLock(currentSection).lockInterruptibly();
//
//            try {
//                // When all the sections had been covered, leave the roundabout
//                roundabout.leaveSection(currentSection);
//                roundabout.log(name + " exited at section #" + currentSection);
//            } finally {
//                roundabout.getLock(currentSection).unlock();
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}
//
