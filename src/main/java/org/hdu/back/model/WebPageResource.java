package org.hdu.back.model;

import java.util.Date;

public class WebPageResource {
    private Long id;

    private String url;

    private Short resourceType;

    private Date crawlTime;

    private Date updateTime;

    private String resourceUrl;
    
    public WebPageResource() {
	}

    public WebPageResource(String url, Short resourceType, Date crawlTime, String resourceUrl) {
		super();
		this.url = url;
		this.resourceType = resourceType;
		this.crawlTime = crawlTime;
		this.resourceUrl = resourceUrl;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public Short getResourceType() {
        return resourceType;
    }

    public void setResourceType(Short resourceType) {
        this.resourceType = resourceType;
    }

    public Date getCrawlTime() {
        return crawlTime;
    }

    public void setCrawlTime(Date crawlTime) {
        this.crawlTime = crawlTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl == null ? null : resourceUrl.trim();
    }
}