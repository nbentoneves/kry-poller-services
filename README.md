# KRY code assignment

One of our developers built a simple service poller. The service consists of a backend service written in
Vert.x (https://vertx.io/) that keeps a list of services (defined by a URL), and periodically does a HTTP GET to each
and saves the response ("OK" or "FAIL").

Unfortunately, the original developer din't finish the job, and it's now up to you to complete the thing. Some of the
issues are critical, and absolutely need to be fixed for this assignment to be considered complete. There is also a
wishlist of features in two separate tracks - if you have time left, please choose *one* of the tracks and complete as
many of those issues as you can. Feel free to use any library or framework for the frontend, or none at all.

Critical issues (required to complete the assignment):

- Whenever the server is restarted, any added services disappear
- There's no way to delete individual services
- We want to be able to name services and remember when they were added
- The HTTP poller is not implemented

wFrontend/Web track:

- We want full create/update/delete functionality for services
- The results from the poller are not automatically shown to the user (you have to reload the page to see results)
- We want to have informative and nice looking animations on add/remove services

Backend track

- Simultaneous writes sometimes causes strange behavior
- Protect the poller from misbehaving services (for example answering really slowly)
- Service URL's are not validated in any way ("sdgf" is probably not a valid service)
- A user (with a different cookie/local storage) should not see the services added by another user

Spend maximum four hours working on this assignment - make sure to finish the issues you start.

Put the code in a git repo on GitHub and send us the link (alex.sadler@kry.se) when you are done.

Good luck!

# Building

We recommend using IntelliJ as it's what we use day to day at the KRY office. In intelliJ, choose

```
New -> New from existing sources -> Import project from external model -> Gradle -> select "use gradle wrapper configuration"
```

You can also run gradle directly from the command line:

```
./gradlew clean run
```
----

# UI + WebService

Running the frontend and the webservice please use the following commands:

UI: ```cd webroot && npm install && npm run dev```

Webservice:```./gradlew clean run```

To access the front end please use the following link: http://localhost:3000/

# Services

#### List of Services - Service to retrieve list of services

```
curl --location --request GET 'http://localhost:8080/service'
```

#### Update Service - Service to update url of service

```
curl --location --request PUT 'http://localhost:8080/service?name=http://google.com' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "https://google.com"    
}'
```

#### Delete Service - Service to delete a service

```
curl --location --request DELETE 'http://localhost:8080/service?name=https://google.pt'
```

#### Insert Service - Service to insert new service

```
curl --location --request POST 'http://localhost:8080/service' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "https://google.com"    
}'
```

