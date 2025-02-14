package etsf20.basesystem.domain.models;

import java.util.UUID;

public class Project {
    private UUID uuid;
    private String projectName;
    private String description;

    public Project(UUID uuid, String projectName, String description){
        this.uuid = uuid;
        this.projectName = projectName;
        this.description = description;
    }

    public String getProjectName(){
        return projectName;
    }

    public String getDescription(){
        return description;
    }

    public UUID getUuid(){
        return uuid;
    }

    public void setProjectName(String projectName){
        this.projectName = projectName;
    }

    public void setDescription(String description){
        this.description = description;
    }

}
