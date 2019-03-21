# KRY code assignment

One of our developers built a simple service poller.
The service consists of a backend service written in Vert.x (https://vertx.io/) that keeps a list of services (defined by a URL), and periodically does a HTTP GET to each and saves the response ("OK" or "FAIL").

Unfortunately, the original developer din't finish the job, and it's now up to you to complete the thing.
In the backlog are the following issues:

- Whenever the server is restarted, any added services disappear
- Service URL's are not validated in any way ("sdgf" is _not_ up)
- There's no way to delete individual services
- The poller doesn't actually work
- The frontend is really ugly, and not fully functional
- Simultaneous writes sometimes causes strange behavior
- There's only one test and it only tests the GET endpoint

Spend maximum four hours working on these issues - make sure to finish the ones you start.

Put the code in a git repo on GitHub and send us the link (niklas.holmqvist@kry.se) when you are done.

Good luck!

# Building
We recommend using IntelliJ as it's what we use day to day at the KRY office.
In intelliJ, choose
```
New -> New from existing sources -> Import project from external model -> Gradle
```

You can also run gradle directly from the command line:
```
./gradlew clean run
```

# Edda's list

done
- make poller work
- make fronted functional
- validate
- save services, restarting server..
- update on frontend periodically
- delete individual services
- refactor

to do

- simultaneous writes to server and file to save services
- test
- make pretty
