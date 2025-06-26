package com.example.Job.Offers.Aggregator.model;


import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "vacancies")
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "external_id")
    private String externalId;
    private String title;
    private String company;
    private String salary;
    private String link;
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Vacancy(Long id, String externalId, String title, String company, String salary,
                   String link, LocalDateTime publishedAt, User user) {
        this.id = id;
        this.externalId = externalId;
        this.title = title;
        this.company = company;
        this.salary = salary;
        this.link = link;
        this.publishedAt = publishedAt;
        this.user = user;
    }

    public Vacancy() { }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
