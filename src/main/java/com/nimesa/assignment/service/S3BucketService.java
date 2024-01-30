package com.nimesa.assignment.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.nimesa.assignment.model.S3Buckets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class S3BucketService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public String updateS3Bucket(String bucketName, List<String> fileNames) {
        Query query = new Query(Criteria.where("bucketName").is(bucketName));
        S3Buckets s3Bucket = mongoTemplate.findOne(query, S3Buckets.class);
        if (s3Bucket != null) {
            s3Bucket.setFileNames(fileNames);
            mongoTemplate.save(s3Bucket);
            return "List of Files Successfully Persisted in Database";
        } else {
            return "String" + bucketName + "was not found";
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
