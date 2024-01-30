package com.nimesa.assignment.service;

import com.nimesa.assignment.model.S3Buckets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
