#!/bin/sh

docker run -d --net=mydemo --name fitnesse -p 8011:8011 mydemo/fitnesse:latest