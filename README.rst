===============================================
CarRaceDashBoard - Real Time Race Visualisation
===============================================

.. contents::

Project Objectives
~~~~~~~~~~~~~~~~~

The goal of the project is to **follow the competitors of a race in real time**.
The following informations are displayed :

- A map with the track and all competitors position,
- The list of competitors, sorted by position with some statistical information,
- Some real time information of a selected driver : speed, distance from departure, lap number

The application has been written as generically as possible. 

- You only need to provide a kml file to display a specific track
- New car information are displayed dynamically when information on a new
  car are received
- The application can easily be modified to follow any type of event
  - Running,
  - Biking,
  - etc...

For the purpose of the demonstration the application is divided is two parts:

- The Real Time Dashboard itself
- The car race simulation that aims to be as realistic has possible.

Benefits and drawback
~~~~~~~~~~~~~~~~~~~~

The major benefit of this application is that it can display real time information in a standard web browser
without requiring polling. All information are updated as soon as they change on the server.

The current implementation is not completely optimised in terms of communication between client and server.
For example, the speed, distance and position informations are pushed separately for the same car, even if
we know that when one changes, the two other change too. We can optimize by sending all three info in the same
event, but we found this implementation more elegant for the purpose of this contest.

Even if it is not useful for a real use of this application, we found **the race simulation part a good use case
to show the benefit of using Scala and Akka.**

High level design and architecture
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Race Dashboard
--------------

*TO BE COMPLETED*

Race Simulation
---------------

*TO BE COMPLETED*


Instructions
~~~~~~~~~~~~

*TO BE COMPLETED*
