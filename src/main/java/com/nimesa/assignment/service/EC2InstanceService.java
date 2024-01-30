package com.nimesa.assignment.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.AmazonEC2AsyncClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.nimesa.assignment.model.EC2Instances;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EC2InstanceService {
    @Autowired
    private MongoTemplate mongoTemplate;

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
}
