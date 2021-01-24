# RSS feed aggregator

This project is an implementation of a basic REST API for a web syndication aggregator.

### Context

The user may subscribe to an arbitrary number of web syndication feeds (Channels) in any of the 
most common formats: RSS1.0, RSS2.0 or ATOM). The user can perform basic CRUD operations over 
this channels, being able to subscribe (POST), edit (PUT), unsubscribe (DELETE) or fetch (GET) the 
contents of the channel's feed.

Beside the channel's CRUD operations, the user has to be able to fetch the channel's up to date feed to retrieve the 
newly published entries. It is also possible to perform an aggregation of the subscribed channels,
having a personal feed with the combined available content among all the subscribed channels.

Channel subscriptions have a TTL to indicate how often the feed must be fetch for updates. When fetching or 
aggregating channels, if a given channel's TTL has not yet expired, it won't be refreshed unless forced by the client.
 
 ### Prerequisites
 
 * JVM 11
 * JDK 11 when building from sources
 * Gradle 6 (A Gradle wrapper is provided with the project)
 * Docker
 
 ### Stack
 
 * Java 11
 * Spring Boot 2.4.2
 * H2 Database
 * MongoDB
 * JUnit5
 * Mockito
 * MockMVC
 * ROME tools (Visit the [Project's git site](https://rometools.github.io/rome/))
 
 ### Installation
 
 To install from sources:
 
 * Download or clone the repository
 
        git clone https://github.com/lusorio/feed-aggregator.git
            
 * Run it with Spring Boot
    
        ./gradlew bootRun
        
    **Note** thata local installation of MongoDB is needed in order to store the fetched entries
    
    
 ### Testing the application
 
  `./gradlew test jacocoTestReport`
    
 ### Running in Docker
 
 A `Dockerfile` is provided with the project in order to build a Docker container.
 
 * Run `./gradlew build`. This will generate the application's `jar` file under `build/libs/*.jar"`
 
 * In the project's root directory (where the Dockerfile file is located), run `docker build -t assignment/aggregator .` to build the Docker image. `assignment/aggreagator` is just a commodity tag name. Use whatever you like to name the application.
 
 * Run the container with `docker run -p 8080:8080 assignment/aggregator` if a local installation of MongoDB is already present.
 
 * Alternatively, use the provided `docker-compose` file. When in the project's root folder, execute
 
        docker-compose up
        
    This will create and run two different containers running both the application's API and a MongoDB instance.
        
 
 * The api will be accessible at htt://localhost:8080/api/. You can try to fetch the list of subscriptions with `curl GET http://localhost:8080/api/channel/`
 
 ### Usage
    
  Please refer to the API documentation for details http://localhost:8080/api/swagger-ui.html
  
  #### Creating a channel
  
  You can create a subscription by performing an HTTP `POST` request to the `channel` endpoint of API 
  with the following payload:
  
      `{
        "name": string,
        "url": string,
        "ttl": long
      }`
  
 
  i.e:

    curl -d '{"name":"Slashdot", "url":"http://rss.slashdot.org/Slashdot/slashdot", "ttl":3600}' -H "Content-Type: application/json" -X POST http://localhost:8080/api/channel/

  **Note:**
  
  * If no channel name is provided by the user, the feed title will be used as default name
  * If no TTL is provided, a default value will be used. The channel will always be refreshed.

  #### Fetching a channel
  
  You can fetch a subscription by performing an HTTP `GET` request to the `channel` endpoint of API 
      
   i.e: 
      
    curl GET http://localhost:8080/api/channel/
  
  #### Updating a channel
  
  You can update a subscription by performing an HTTP `PUT` request to the `channel` endpoint of API 
    with the following payload:
    
      {
        "name": string,
        "url": string,
        "ttl": long
      }
    
  i.e:
  
    curl -d '{"name":"Slashdot", "url":"http://rss.slashdot.org/Slashdot/slashdot", "ttl":3600}' -H "Content-Type: application/json" -X POST http://localhost:8080/api/channel/{channelId}
  
  **Note:** 
  
  Subscription URLs can't be modified. Thus, a new subscription must be done if a new URL needs to be used.
  
  #### Deleting a channel
  
   You can delete a subscription by performing an HTTP `GET` request to the `channel` endpoint of API 
   
   i.e:
        
    curl DELETE http://localhost:8080/api/channel/{channelId}
   
   #### Fetching a channel's feed
 
   You can update a channel's feed by performing an HTTP `GET` request to the `feed/channel` endpoint of API 
     
  i.e: 
     
    curl GET http://localhost:8080/api/feed/channel/{channelId}?forceRefresh=true|false
  
  #### Aggregating channels
   
  In order to aggregate the different feeds, you can perform an HTTP `GET` request to the `feed/aggregate` endpoint of API 
       
  i.e: 
       
    curl GET http://localhost:8080/api/feed/aggregate/?forceRefresh=true|false
    


### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.4.2/gradle-plugin/reference/html/)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/2.4.2/reference/htmlsingle/#boot-features-jpa-and-spring-data)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.4.2/reference/htmlsingle/#boot-features-developing-web-applications)
* [Rome](https://rometools.github.io/rome/)