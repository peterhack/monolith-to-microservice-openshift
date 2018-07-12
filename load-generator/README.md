# Load Generation Project for TicketMonster

This project provides load generation scripts for TicketMonster. These scripts simulate real user actions by utilizing the [PhantomJS](http://phantomjs.org/download.html) and [CasperJS](http://casperjs.org/) framework. While PhantomJS is a headless web browser scriptable with JavaScript, CasperJS allows you to build full navigation scenarios.

You don't need to install PhantomJS or CapserJS, as long as you follow the instructions building a Docker container, which provides a running environment. 

## Prerequisites

* You need [Docker](https://www.docker.com/community-edition) to create a Docker image 

## Scripts

### click_through.js
This script navigates through TicketMonster by clicking the Event, Venue, and Booking link in the top menu. As browser the script uses  

## Instructions

1. [Clone the repository](https://github.com/dynatrace-innovationlab/monolith-to-microservice-openshift#instructions) and change directory**
   ```sh
   $ cd load-generation
   ```
1. Build Docker image
   ```sh
   docker build -t <your dockerhub account>/loadgeneration:latest .
   ```

3. Run container and start script
   ```sh
   usage: loadgeneration.sh <target URL> <duration in minutes>  <[clients/minute] | [empty = random number of clients (1..10) / minute] >
  
   docker run -d --rm <image> /bin/bash loadgeneration.sh https://<yourURL>/ 10 5
   ```