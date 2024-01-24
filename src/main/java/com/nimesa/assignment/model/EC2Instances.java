package com.nimesa.assignment.model;

import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Document("ec2instances")
public class EC2Instances {
    @Id
    private String instanceId;
    private String imageId;
    private String instanceType;
    private String state;
    private String monitoringState;


    public EC2Instances(String instanceId, String imageId, String instanceType, String state, String monitoringState) {
        this.instanceId = instanceId;
        this.imageId = imageId;
        this.instanceType = instanceType;
        this.state = state;
        this.monitoringState = monitoringState;
    }
}
