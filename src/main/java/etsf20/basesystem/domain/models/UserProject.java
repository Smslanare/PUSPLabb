package etsf20.basesystem.domain.models;

public class UserProject {
    private String username;
    private String project_uuid;

    public UserProject(String username, String projectId) {
        this.username = username;
        this.project_uuid = projectId;
    }

    public String getUsername() {
        return username;
    }

    public String getProjectId() {
        return project_uuid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProjectId(String project_uuid) {
        this.project_uuid = project_uuid;
    }
}