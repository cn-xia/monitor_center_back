package org.hdu.back.model;

import java.util.Date;

public class JobDaily {
    private Integer id;

    private Integer jobId;

    private Date startTime;

    private Date endTime;

    private String spendTime;

    private String crawlerTime;

    private Integer totalCount;

    private Long totalSold;

    private Date createTime;

    private Integer jobInterval;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getSpendTime() {
        return spendTime;
    }

    public void setSpendTime(String spendTime) {
        this.spendTime = spendTime == null ? null : spendTime.trim();
    }

    public String getCrawlerTime() {
        return crawlerTime;
    }

    public void setCrawlerTime(String crawlerTime) {
        this.crawlerTime = crawlerTime == null ? null : crawlerTime.trim();
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Long getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(Long totalSold) {
        this.totalSold = totalSold;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getJobInterval() {
        return jobInterval;
    }

    public void setJobInterval(Integer jobInterval) {
        this.jobInterval = jobInterval;
    }
}