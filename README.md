# RSS feed aggregator

This project is an implementation of a basic REST API for a web syndication aggregator.

### Context

The user may subscribe to an arbitrary number of web syndication feeds (Channels) in any of the 
most common formats: RSS1.0, RSS2.0 or ATOM). He or she may need to perform basic CRUD operations over 
this channels, being able to subscribe (POST), edit (PUT), unsubscribe (DELETE) or fetch (GET) the 
contents of the feed.

Beside this channel operations, the user need to be able to perform an aggregation of the subscribed channels,
having a personal feed with the available content among the subscribed channels.

Channel subscriptions have a TTL to indicate how often the feed must be fetch for updates. When fetching or 
aggregating channels, if a given channel's TTL has not yet expired, it won't be refreshed.
 
 ### Prerequisites
 
 * JVM 11
 * JDK 11 when building from sources
 * Gradle 6 (A Gradle wrapper is provided with the project)
 
 ### Stack
 
 * Java 11
 * Spring Boot 2.4.2
 * H2 Database
 * JUnit5
 * Mockito
 * MockMVC
 * Rometools (Visit the [Project's git site](https://rometools.github.io/rome/))
 
 ### Installation
 
 To install from sources:
 
 * Download or clone the repository
 
    git clone https://github.com/lusorio/feed-aggregator.git
    
 * Run it with Spring Boot
    
    ./gradlew bootRun
    
 #### Running in a different servlet container
 
 * Install the maven project
     
     ./gradlew build
        
 * Deploy the generated .war artifact in the webapps (or equivalent) directory
         
 ### Test the application
 
 To run the test suite:
        
    gradle test
 
 ### Architecture
 
 #### Configuration
  
  A configuration file [application.properties](https://github.com/lusorio/feed-aggregator/blob/master/src/main/resources/application.properties) is provided in the src/main/resources directory.
  
 #### Database and data access layer
 This projects uses an H2 embedded DB to keep track of the subscribed channels.
 
 Hibernate is used through the Spring JPA framework in the data access layer, under the `repositories` package. Repositories
 handle the state of the entities represented in the `models` package.
 
 #### Service layer
 
 Domain logic sits under the `services` package.
 
 The ROME library is used as a web syndication feed client. This library provides an easy way to fetch web feeds in a format
 agnostic way. It parses XML documents and builds a custom SyndFeed object which holds the feed values along with its entries,
 for every one of the most common formats (RSS1.0 and 2.0 families and ATOM)
 
 #### API
 
 The API exposes the aggregated feed as a JSON document. It uses a simplified model of the SyndEntryImpl.java class provided
 by Rome which holds only basic feed values.
 
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