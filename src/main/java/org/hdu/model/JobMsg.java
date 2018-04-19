package org.hdu.model;

import java.util.Date;

public class JobMsg {
    private Integer id;

    private Integer jobId;

    private Integer crawlerCount;

    private Integer incCrawlerCount;

    private Integer saveCount;

    private Integer incSaveCount;

    private String cpu;

    private String ram;

    private Date monitorTime;

    private Integer dailyId;

    public JobMsg() {
    }

    public JobMsg(Integer jobId, Integer crawlerCount, Integer incCrawlerCount, Integer saveCount, Integer incSaveCount, String cpu, String ram, Date monitorTime, Integer dailyId) {
        this.jobId = jobId;
        this.crawlerCount = crawlerCount;
        this.incCrawlerCount = incCrawlerCount;
        this.saveCount = saveCount;
        this.incSaveCount = incSaveCount;
        this.cpu = cpu;
        this.ram = ram;
        this.monitorTime = monitorTime;
        this.dailyId = dailyId;
    }

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

    public Integer getCrawlerCount() {
        return crawlerCount;
    }

    public void setCrawlerCount(Integer crawlerCount) {
        this.crawlerCount = crawlerCount;
    }

    public Integer getIncCrawlerCount() {
        return incCrawlerCount;
    }

    public void setIncCrawlerCount(Integer incCrawlerCount) {
        this.incCrawlerCount = incCrawlerCount;
    }

    public Integer getSaveCount() {
        return saveCount;
    }

    public void setSaveCount(Integer saveCount) {
        this.saveCount = saveCount;
    }

    public Integer getIncSaveCount() {
        return incSaveCount;
    }

    public void setIncSaveCount(Integer incSaveCount) {
        this.incSaveCount = incSaveCount;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu == null ? null : cpu.trim();
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram == null ? null : ram.trim();
    }

    public Date getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(Date monitorTime) {
        this.monitorTime = monitorTime;
    }

    public Integer getDailyId() {
        return dailyId;
    }

    public void setDailyId(Integer dailyId) {
        this.dailyId = dailyId;
    }
}