package com.nimesa.assignment.model;

import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.Date;

@Document("s3buckets")
public class S3Buckets {

    @Id
    private String bucketName;
    private Date creationDate;

    public S3Buckets(String bucketName, Date creationDate) {
        this.bucketName = bucketName;
        this.creationDate = creationDate;
    }
}
