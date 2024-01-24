package com.nimesa.assignment.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AWSCredentialsInitializer {

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    public AWSCredentialsData initializeAWSCredentials() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
        Regions awsRegion = Regions.fromName(region);
        return new AWSCredentialsData(credentialsProvider, awsRegion);
    }

    public static class AWSCredentialsData {
        private final AWSStaticCredentialsProvider credentialsProvider;
        private final Regions awsRegion;

        public AWSCredentialsData(AWSStaticCredentialsProvider credentialsProvider, Regions awsRegion) {
            this.credentialsProvider = credentialsProvider;
            this.awsRegion = awsRegion;
        }

        public AWSStaticCredentialsProvider getCredentialsProvider() {
            return credentialsProvider;
        }

        public Regions getAwsRegion() {
            return awsRegion;
        }
    }

}
