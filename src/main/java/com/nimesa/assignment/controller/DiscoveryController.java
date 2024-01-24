package com.nimesa.assignment.controller;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.AmazonEC2AsyncClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.nimesa.assignment.model.EC2Instances;
import com.nimesa.assignment.model.S3Buckets;
import com.nimesa.assignment.service.AWSCredentialsInitializer;
import com.nimesa.assignment.service.AsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    public DiscoveryController(AWSCredentialsInitializer awsCredentialsInitializer) {
        this.awsCredentialsInitializer = awsCredentialsInitializer;
    }

    // This API asynchronously discover EC2 instances in the Mumbai Region in one thread and S3 buckets in another thread and persist the result in DB
    @GetMapping("/discover/services")
    public String discoverServices() {

        AWSCredentialsInitializer.AWSCredentialsData awsCredentialsData = awsCredentialsInitializer.initializeAWSCredentials();
        Regions awsRegion = awsCredentialsData.getAwsRegion();
        AWSStaticCredentialsProvider credentialsProvider = awsCredentialsData.getCredentialsProvider();

        AsyncService.submit(() -> describeEC2Instances(credentialsProvider, awsRegion));
        AsyncService.submit(() -> describeS3Instances(credentialsProvider, awsRegion));


        CompletableFuture<Void> ec2Future = describeEC2Instances(credentialsProvider, awsRegion);
        CompletableFuture<Void> s3Future = describeS3Instances(credentialsProvider, awsRegion);

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
        return "Done";
    }

    @GetMapping("/discover/result")
    public List<String> getDiscoveryResult(String service){

        AWSCredentialsInitializer.AWSCredentialsData awsCredentialsData = awsCredentialsInitializer.initializeAWSCredentials();
        Regions awsRegion = awsCredentialsData.getAwsRegion();
        AWSStaticCredentialsProvider credentialsProvider = awsCredentialsData.getCredentialsProvider();
        List<String> ans = new ArrayList<>();
        if(service.contains("S3")){
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withRegion(awsRegion)
                    .build();

            List<Bucket> buckets = s3Client.listBuckets();

            for (Bucket b : buckets) {
                ans.add(b.getName());
            }

        }
        if(service.contains("EC2")){
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

    @Async
    public CompletableFuture<Void> describeEC2Instances(AWSStaticCredentialsProvider credentialsProvider, Regions awsRegion){
        try{
            // Create EC2 Async client
            System.out.println("Thread ec2" + Thread.currentThread());
            AmazonEC2Async ec2AsyncClient = AmazonEC2AsyncClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withRegion(awsRegion)
                    .build();
            boolean done = false;

            DescribeInstancesRequest request = new DescribeInstancesRequest();
            while(!done) {
                DescribeInstancesResult response = ec2AsyncClient.describeInstances(request);

                for(Reservation reservation : response.getReservations()) {
                    for(Instance instance : reservation.getInstances()) {
                        EC2Instances ec2Instance = new EC2Instances(
                                instance.getInstanceId(),
                                instance.getImageId(),
                                instance.getInstanceType(),
                                instance.getState().getName(),
                                instance.getMonitoring().getState()
                        );

                        mongoTemplate.save(ec2Instance);
                    }
                }

                request.setNextToken(response.getNextToken());

                if(response.getNextToken() == null) {
                    done = true;
                }
            }

            ec2AsyncClient.shutdown();
            return CompletableFuture.completedFuture(null);
        } catch (Exception e){
            return CompletableFuture.failedFuture(e);
        }
    }
    @Async
    public CompletableFuture<Void> describeS3Instances(AWSStaticCredentialsProvider credentialsProvider, Regions awsRegion){
        //Create S3 Async client
        try {
            System.out.println("Thread s3" + Thread.currentThread());
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withRegion(awsRegion)
                    .build();

            List<Bucket> buckets = s3Client.listBuckets();
            for (Bucket b : buckets) {

                S3Buckets s3BucketObject = new S3Buckets(
                        b.getName(),
                        b.getCreationDate()
                );

                mongoTemplate.save(s3BucketObject);
            }
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
