## Requirements
This application uses JDK 1.8 .`JAVA_HOME` environment variable must be set properly.
Also before running please add your PipeDrive api token to `PIPEDRIVE_TOKEN` environment variable. Otherwise application would not start.

## How to run
This spring boot application uses maven wrapper, so there is no need to download maven.

Just run `./mvnw -DskipTests spring-boot:run` in the base directory.

Application listens on port `8080`.

## Notes
* My understanding of the task is that for each gist there should be an activity **OR** a deal. And since creating a deal for a gist did not make sense to me, I decided to make an activity.
* From the task I understood that _user_ was a local concept, so I did not create a Pipe Drive `Person` or `User` for the users being screened.
* `DefaultMainService.processGists()` will query new gists for all users in the screening list every hour and add a Pipe Drive activity for each gist.
* The Screening list is the only information worth keeping and using a database for that purpose would only complicate the code. Therefore a simple file is used to keep track of the list. File content is loaded upon service construction and will be overwritten before service destruction. 
* The `users.data` file in the base directory contains the data of users. Removal of this file would result in loss of that data. Application will overwrite the data in this file.

## Endpoints 
1. `GET /users` will return all users being screened in JSON format.
2. `POST /add-user` is used to add a user to screening list. Content-Type of `application/x-www-form-urlencoded` with a `username` parameter is expected.
Duplicate users are not accepted.
3. `DELETE /delete-user/{username}` is used to delete users from list.
4. `GET /{username}/gists` returns all gists of a user with `username` since last visit.