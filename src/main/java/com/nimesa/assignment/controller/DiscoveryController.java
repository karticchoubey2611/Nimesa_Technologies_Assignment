package com.nimesa.assignment.controller;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.AmazonEC2AsyncClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.nimesa.assignment.model.EC2Instances;
import com.nimesa.assignment.model.S3Buckets;
import com.nimesa.assignment.service.AWSCredentialsInitializer;
import com.nimesa.assignment.service.AsyncService;
import com.nimesa.assignment.service.EC2InstanceService;
import com.nimesa.assignment.service.S3BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@Service
@RequestMapping("/api")
public class DiscoveryController {


    private final AWSCredentialsInitializer awsCredentialsInitializer;

    private final S3BucketService s3BucketService;

    private final EC2InstanceService ec2InstanceService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    public DiscoveryController(AWSCredentialsInitializer awsCredentialsInitializer, S3BucketService s3BucketService, EC2InstanceService ec2InstanceService ) {
        this.awsCredentialsInitializer = awsCredentialsInitializer;
        this.s3BucketService = s3BucketService;
        this.ec2InstanceService = ec2InstanceService;
    }


    @GetMapping("/discover/services")
    public String discoverServices() {

        AWSCredentialsInitializer.AWSCredentialsData awsCredentialsData = awsCredentialsInitializer.initializeAWSCredentials();
        Regions awsRegion = awsCredentialsData.getAwsRegion();
        AWSStaticCredentialsProvider credentialsProvider = awsCredentialsData.getCredentialsProvider();

        AsyncService.submit(() -> ec2InstanceService.describeEC2Instances(credentialsProvider, awsRegion));
        AsyncService.submit(() -> s3BucketService.describeS3Instances(credentialsProvider, awsRegion));


        CompletableFuture<Void> ec2Future = ec2InstanceService.describeEC2Instances(credentialsProvider, awsRegion);
        CompletableFuture<Void> s3Future = s3BucketService.describeS3Instances(credentialsProvider, awsRegion);

        System.out.println("EC2 Future" + ec2Future);
        System.out.println("S3 Future" + s3Future);


        ec2Future.thenAccept(result -> {
            System.out.println("EC2 instance description completed successfully.");
        }).exceptionally(ex -> {
            System.err.println("Error describing EC2 instances: " + ex.getMessage());
            return null;
        });

        s3Future.thenAccept(result -> {
            System.out.println("S3 instance description completed successfully.");
        }).exceptionally(ex -> {
            System.err.println("Error describing S3 instances: " + ex.getMessage());
            return null;
        });
        return "EC2 Instances & S3 buckets persisted in Database";
    }

    @GetMapping("/discover/result")
    public List<String> getDiscoveryResult(@RequestParam("serviceType") String serviceType){

        AWSCredentialsInitializer.AWSCredentialsData awsCredentialsData = awsCredentialsInitializer.initializeAWSCredentials();
        Regions awsRegion = awsCredentialsData.getAwsRegion();
        AWSStaticCredentialsProvider credentialsProvider = awsCredentialsData.getCredentialsProvider();
        List<String> ans = new ArrayList<>();
        if(serviceType.contains("S3")){
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withRegion(awsRegion)
                    .build();

            List<Bucket> buckets = s3Client.listBuckets();

            for (Bucket b : buckets) {
                ans.add(b.getName());
            }

        }
        else if(serviceType.contains("EC2")){
            AmazonEC2Async ec2AsyncClient = AmazonEC2AsyncClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withRegion(awsRegion)
                    .build();

            DescribeInstancesRequest request = new DescribeInstancesRequest();
            boolean done = false;
            while(!done) {
                DescribeInstancesResult response = ec2AsyncClient.describeInstances(request);

                for(Reservation reservation : response.getReservations()) {
                    for(Instance instance : reservation.getInstances()) {
                        ans.add(instance.getInstanceId());
                    }
                }
                request.setNextToken(response.getNextToken());

                if(response.getNextToken() == null) {
                    done = true;
                }
            }

        }
        return ans;
    }

    @GetMapping("/discover/s3Objects")
    public String getS3BucketObjects(@RequestParam("bucketName") String bucketName){
        AWSCredentialsInitializer.AWSCredentialsData awsCredentialsData = awsCredentialsInitializer.initializeAWSCredentials();
        Regions awsRegion = awsCredentialsData.getAwsRegion();
        AWSStaticCredentialsProvider credentialsProvider = awsCredentialsData.getCredentialsProvider();

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(awsRegion)
                .build();

        List<String> fileList = new ArrayList<>();
        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName);

        ListObjectsV2Result result;
        do {
            result = s3Client.listObjectsV2(request);

            result.getObjectSummaries().forEach(object -> fileList.add(object.getKey()));

            String token = result.getNextContinuationToken();
            request.setContinuationToken(token);
        } while (result.isTruncated());

        return s3BucketService.updateS3Bucket(bucketName, fileList);


    }

    @GetMapping("discover/s3ObjectsCount")
    public String getS3BucketObjectCount(@RequestParam("bucketName") String bucketName){
        Query query = new Query(Criteria.where("bucketName").is(bucketName));
        S3Buckets s3Bucket = mongoTemplate.findOne(query, S3Buckets.class);
        if(s3Bucket != null){
            List<String> fileNames = s3Bucket.getFileNames();
            return "The count of Objects in " + bucketName + " is " + fileNames.size();
        } else {
            return "String" + bucketName + "was not found";
        }
    }

    @GetMapping("/discover/s3ObjectsLike")
    public List<String> getS3BucketObjectLike(
            @RequestParam("bucketName") String bucketName,
            @RequestParam("pattern") String pattern) {

        AWSCredentialsInitializer.AWSCredentialsData awsCredentialsData = awsCredentialsInitializer.initializeAWSCredentials();
        Regions awsRegion = awsCredentialsData.getAwsRegion();
        AWSStaticCredentialsProvider credentialsProvider = awsCredentialsData.getCredentialsProvider();

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(awsRegion)
                .build();

        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(pattern);
        ListObjectsV2Result result = s3Client.listObjectsV2(request);

        List<String> objectKeys = new ArrayList<>();
        for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
            objectKeys.add(objectSummary.getKey());
        }

        return objectKeys;
    }
}
