package ru.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="User_Questions")
public class UserQuestion extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID", nullable=false)
    private Long id;

    @Column(name="Question",nullable = false, columnDefinition = "nvarchar(500)")
    private String question;

    @Column(name="Answer", columnDefinition = "nvarchar(2000)")
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="MAX_ID", referencedColumnName = "MAX_ID",nullable = false)
    private User user;

    @Column(name="Date")
    private LocalDateTime questionCreationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getQuestionCreationDate() {
        return questionCreationDate;
    }

    public void setQuestionCreationDate(LocalDateTime questionCreationDate) {
        this.questionCreationDate = questionCreationDate;
    }
}
