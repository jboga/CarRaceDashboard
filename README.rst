===============================================
CarRaceDashBoard - Real Time Race Visualisation
===============================================

.. contents::

Project Objectives
~~~~~~~~~~~~~~~~~

This project has been developed by Antoine Detante `(@antoined) <http://twitter.com/antoined>`_ and
Fabrice Croiseaux `(@fXzo) <http://twitter.com/fXzo>`_ for the purpose 
of the Typesafe Developer Contest (see details at http://typesafe.com/resources/developer-contest).

The goal of the project is to **follow the competitors of a race in real time**.
The application consists in a Dashboard that displays the following information :

- A map with the track and all competitors position,
- The list of competitors, sorted by position with some statistical information,
- Some real time information of the selected driver : speed, distance from departure, lap number.

The application has been written as generically as possible. 

- You only need to provide a kml file to display a specific track,
- information are displayed dynamically when information on a new car are received,
- The application can easily be modified to follow any type of event :
  - Running,
  - Biking,
  - etc...
  We plan to develop an iPhone app that will send position, distance and speed information to our application
  and we will use it to follow a famous moutain bike race that is organised every year in our region
  (http://www.24hvttcrapauds.eu/cms/index.php/24h-vtt-les-crapauds.html)

For the purpose of the demonstration the application is divided in two parts:

- The Real Time Dashboard itself,
- The car race simulation that aims to be as realistic has possible while remaining simple.

Benefits and drawback
~~~~~~~~~~~~~~~~~~~~

The major benefit of this application is that it can display real time information in a standard web browser
without requiring polling. All information are updated as soon as they change on the server.

The current implementation is not completely optimised in terms of communication between client and server.
For example, the speed, distance and position informations are pushed separately for the same car, even if
we know that when one changes, the two other change too. We could optimize by sending all three info in the same
event, but we found this implementation more elegant for the purpose of this contest.

Even if it is quite simple and not useful for a real use, we found **the race simulation part a good use case to show the
benefits of using Scala and Akka.** To make it realistic, we have selected a min speed and a max speed and the speed of
each vehicle vary between both values. We didn't took care of the track configuration and it may happen that a
car take a tight turn at 200km/h.

High level design and architecture
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The application is divided into 2 logical parts:

- The simulation part, which produces events based on a random-but-realistic implementation
- The dashboard part, which retrieves these events, and publishes them in real time to a web interface

In a real world application, the simulation part will be replaced by real-time datas coming from physical
captors in each cars by example. The aim of the simulation part is to generate a lot of events (instant speed, position, ...).


Race Simulation
---------------

The race simulation code is defined in the "simulation" package. 
There are 3 main components : 

- A parser of KML files : this class reads a KML file and transforms it to a list of points. This list is used by
  the simulation code to calculate positions of cars on the track.
- A list of actors (one for each car in the race), which are scheduled at fixed rate and compute the new position
  of the car. This position is computed by adding or removing a random number to the current car's speed.
- A list of Enumerators, which retrieves the state of each cars and produces different types of events (position event,
  speed event, ...). All these enumerators of events are interleaved in a global eumerator, which produces all event
  types for all cars.

Race Dashboard
--------------

This part contains two global actors. The first one is the Storage Actor which is connected to the global
enumerator described above. For each event in this stream, a message is sent to the Storage Actor.
It stores the data of the event in a Mongo database for later use, and publishes this event to the Actors
eventStream.

All events are displayed in the console in development mode. In production, they are not displayed.

The second actor is scheduled at fixed rate. It compute some statistics (max speed, average speed, ...) with queries
in the Mongo database and publishes these results to the Actor eventStream.

Finally, we have also in this part the web interface and controllers which come with. Some methods of the controller
are used to manage the simulation (start a new race, stop the current race). One method is called to render the web
interface. Another is used as a Event Source for a Server Sent Events stream opened by the web client. In this method,
a new Actor is instanciated for each web client who connects to the race. This actor is a listener, which is subscribed
to the eventStream and publishes a JSONified message to the SSE socket for each received event.

Unfortunately, we couldn't use the reactivemongo driver in this application because it requires Play 2.1. We'll update
the application to use it as soon as Play 2.1 is released in the Typesafe Stack.

The client application is developed in Coffeescript with backbone.js and Twitter Bootstrap. We love the integration
of Coffeescript in Play.

Instructions
~~~~~~~~~~~~

The application needs a MongoDB server (latest release 2.2.1) running at localhost on the default port (27017).
To start the application locally, type the following line in the root directory of the application : ::

  sbt run

The application is now available on http://localhost:9000

Another way to test the application is to go to the public demo at http://carracedashboard.trustedpaas.lu.
Don't forget to stop the race if you use the public link to test the application.

====================
**HAVE A GOOD RACE**
====================
