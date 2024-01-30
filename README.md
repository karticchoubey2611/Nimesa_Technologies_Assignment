# Nimesa_Technologies_Assignment

## Setup Instructions

1. Clone the Repository
2. cd Nimesa_Technologies_Assignment
3. Make sure you have JDK 17 Installed
4. Add Spring Boot URL in application.properties (Explained Below)
5. Add AWS Creds in application.yml (Explained Below)
4. Run the project on Intellij/Eclipse


## Introduction

This Assignment have REST APIs, Model and Services implemented. I am using MongoDB as a database as we don't need to define the whole structure of the tables. Also, it is easy to query on MongoDB clusters.
Access Key and Secret Key was provided by Nimesa in a document. We can easily use those credentials in application.yml.

I have exposed some REST API which are as follows

1. `discoverServices` - This API asynchronously persists the EC2 Instances and S3 buckets present in Mumbai Region (ap-south-1) in different 
2. `getDiscoveryResult` - This API will list EC2 Instances/S3 Bucket according to the serviceType provided in request query.
3. `getS3BucketObjects` - This API will persist the folders and file inside the S3 Bucket into MongoDB collection. We have to give bucket name as a request query.
4. `getS3BucketObjectCount` - This API will give the count of Object inside a S3 bucket. We have to give bucket name as a request query.
5. `getS3BucketObjectLike` - This API will list the folders and files inside the S3 bucket which starts with a string pattern. We have to give S3 bucket name and pattern as a request.

APIs are implemented in DiscoveryController.java

There are 2 Models in the application - 

1. EC2Instances - This will store the EC2 instances and necessary details related to it.
2. S3Buckets - This will store the S3 Buckets and necessary details related to it.

`I have taken the screenshots of the API response which is inside api_output_images folder.`

There are some services as well that I have created which is used by APIs.


## application.properties

In this you have to set the mongodb connection string. application.properties should be present in `src/main/resources` .

Your application.properties should look like this - 

```
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster0.yyfh1dj.mongodb.net/?retryWrites=true&w=majority
spring.data.mongodb.database=nimesa-assignment

```

Create your own MongoDB cluster for the java project and paste the connection string in application.properties file.

## application.yml

I have set AWS creds in application.yml which should be present in `src/main/resources`.

Your application.yml should look like this - 

```
aws:
  accessKey: <YOUR_ACCESS_KEY>
  secretKey: <YOUR_SECRET_KEY>
  region: ap-south-1
```


NOTE: THIS WHOLE ASSIGNMENT I DID WITH MY OWN ASSUMPTIONS AND WITHOUT THE GUIDANCE OF ANYONE. JUST TOOK THE HELP OF INTERNET + CHATGPT. ALSO THERE ARE SOME THINGS ON WHICH I GOT STUCK AND COULDN'T ABLE TO IMPLEMENT IT PROPERLY, FOR EXAMPLE , IN THE PROBLEM STATEMENT, I WAS NOT ABLE TO UNDERSTAND THE JOB ID PART. I AM THINKING IT IS SOMETHING RELATED TO ASYNCHRONOUS PROGRAMMING. I WAS TRYING TO THINK ABOUT IT BUT COULDN'T ABLE TO FIGURE IT OUT.
