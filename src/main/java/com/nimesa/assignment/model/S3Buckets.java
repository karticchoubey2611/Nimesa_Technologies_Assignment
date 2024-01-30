package com.nimesa.assignment.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.Date;
import java.util.List;

@Document("s3buckets")
public class S3Buckets {

    @Id
    private String bucketName;
    private Date creationDate;

    private List<String> fileNames;

    @Autowired
    private MongoTemplate mongoTemplate;

    public S3Buckets(){

    }

    public S3Buckets(String bucketName, Date creationDate) {
        this.bucketName = bucketName;
        this.creationDate = creationDate;
    }

    public S3Buckets(String bucketName, Date creationDate, List<String> fileNames) {
        this.bucketName = bucketName;
        this.creationDate = creationDate;
        this.fileNames = fileNames;
    }

    public String getBucketName() {
        return bucketName;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }
}
