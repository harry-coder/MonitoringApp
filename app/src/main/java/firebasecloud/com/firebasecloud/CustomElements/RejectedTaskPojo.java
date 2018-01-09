package firebasecloud.com.firebasecloud.CustomElements;

/**
 * Created by Harpreet on 07/01/2018.
 */

public class RejectedTaskPojo {

    String title;
    String description;
    String startDate;
    String incentive;
    String taskId;

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTaskExpire() {
        return taskExpire;
    }

    public void setTaskExpire(String taskExpire) {
        this.taskExpire = taskExpire;
    }

    String endDate;
    String taskExpire;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getIncentive() {
        return incentive;
    }

    public void setIncentive(String incentive) {
        this.incentive = incentive;
    }



}
