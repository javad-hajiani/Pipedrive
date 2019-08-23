[![Build Status](https://travis-ci.org/javad-hajiani/Pipedrive.svg?branch=master)](https://travis-ci.org/javad-hajiani/Pipedrive)

[![Octocat](https://github.githubassets.com/images/icons/emoji/octocat.png)](./somelink)

# Part I - Github API & Pipedrive API

### Requirements
This application uses JDK 1.8 .`JAVA_HOME` environment variable must be set properly.
Also before running please add your PipeDrive api token to `PIPEDRIVE_TOKEN` environment variable Or you can run it with `docker-compose` without install JDK and Environment locally. Otherwise application would not start.

### How to run
There is two way to run application on your own system . 

###### Method 1:
This application able to run from docker so you can run it with below command and it will run on your system :) .

 > `docker-compose up -d`

Application listens on port `8080`.


###### Method 2:

This spring boot application uses maven wrapper, so there is no need to download maven.

Just run `./mvnw -DskipTests spring-boot:run` in the base directory.

Application listens on port `8080`.

### Notes
* My understanding of the task is that for each gist there should be an activity **OR** a deal. And since creating a deal for a gist did not make sense to me, I decided to make an activity.
* From the task I understood that _user_ was a local concept, so I did not create a Pipe Drive `Person` or `User` for the users being screened.
* `DefaultMainService.processGists()` will query new gists for all users in the screening list every 3 hours and add a Pipe Drive activity for each gist.
* The Screening list is the only information worth keeping and using a database for that purpose would only complicate the code. Therefore a simple file is used to keep track of the list. File content is loaded upon service construction and will be overwritten before service destruction. 
* The `users.data` file in the base directory contains the data of users. Removal of this file would result in loss of that data. Application will overwrite the data in this file.

### Endpoints 
1. `GET /users` will return all users being screened in JSON format.
2. `POST /add-user` is used to add a user to screening list. Content-Type of `application/x-www-form-urlencoded` with a `username` parameter is expected.
Duplicate users are not accepted which returns `201 Created` http response code for successful operation.
3. `DELETE /delete-user/{username}` is used to delete users from list  which returns `202 Accepted` http response code for successful operation.
4. `GET /{username}/gists` returns all gists of a user with `username` since **last visit**.
5. `GET /actuator/info` returns application information.
6. `GET /actuator/health` returns application health in JSON format.

also if you feel the need to more details you are able to generate javadoc to get deeper.

# Part II - CI /CD

### Travis
I used travis for CI/CD process and defined `PIPEDRIVE_TOKEN` and `GCLOUD_SERVICE_KEY` 
private **ENVIRONMENT VARIABLES** on it. I have been choose travis because I know it better than else and it meets bonus point by itself but we can trigger other CI/CD tool like (Jenkins) with webhooks in GITHUB.

I wrote some unit tests which cover my application functionalities so I run it in CI section and after that Deploy it to **GKE**(Google Cloud Kubernetes Service).

There is no need to manual work to deploy, if you merge or commit to master branch it will test and deploy on cloud, In this project I used github flow also after successful/failure deployment we receive email for it

# Part III - The cloud

I Dockerized my code and Build it multi stage because it brings a lot of advantages like **Security**,Lightweight Image which make our deployment faster,so as you know we don't have our source code on Cloud, we bundle it in jar file and run it with `java:jre` container.
I have chosen kubernetes for infrastructure because it helps us make our application scalable 

### Infrastructure as Code
I choose Terraform for provision our infrastructure because it has big community and it's stable to now.
Actually we have no manual work even for implement our production environment, here we go
###### Step 1 ( Install Terraform ):
Download terraform from below link and extend path to your bash path variable
<a href="https://www.terraform.io/downloads.html">**Download HERE**</a>

###### Step 2 ( Download Google Cloud Service Key and Enable kubernetes API ):
We need a way for the Terraform runtime to authenticate with the GCP API so go to the 
**Cloud Console**, navigate to **IAM & Admin** > **Service Accounts**, and click Create Service Account with Project Editor role. 
Your browser will download a JSON file containing the details of the service account and a private key that can authenticate as a project editor to your project. 
Keep this JSON file safe! 
```bash
cd deployment/iac
mkdir creds
cp DOWNLOADEDSERVICEKEY.json creds/serviceaccount.json
terraform apply
```

If you have questions Feel free to ask
Thanks 

