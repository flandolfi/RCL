Assignment - Week 4
===================

Matrices Multiplication
-----------------------

Project files:

 - `Multiplier.java`;
 - `Test.java`;
 - `out.txt`.


Use the commands below to compile/run the multiplication task (pretty useless):

    $ javac Multiplier.java
    $ java Multiplier [THREADS_COUNT]

where `THREADS_COUNT` is the number of threads in the pool.

Use instead the commands below to launch the test suite (and the performance
analysis):

    $ javac Test.java
    $ java Test


### Benchmark results ###

Running the test suite I got the following results (see `out.txt` for details):

 Threads  |  Average Elapsed Time (of 3 tests)
--------- | -----------------------------------
    1     |            14440ms
    2     |             7527ms
    3     |             4836ms
    4     |             6125ms
    5     |             4446ms
    6     |             4960ms
    7     |             2271ms
    8     |             2600ms
    9     |             4002ms

There is a speedup between 1 and 2 threads (~1.92x), 2 and 3 threads (~1.56x)
and then another one between 6 and 7 threads (~2.18x).

The best result is obtained with 7/8 threads (probably depending on number of
cores -- mine is 8).

There is no significant improvement in using more than 8 threads (average
elapsed time remains around 4-5 seconds).



Dining Hall
-----------

Project files:

 - `RinsingTask.java`;
 - `Student.java`;
 - `WashingTask.java`.


Use the commands below to compile/run the project:

    $ javac Student.java
    $ java Student


