package ru.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name="FAQ")
public class FAQ extends PanacheEntityBase {

    @Id
    @Column(name="ID", nullable=false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="Question",nullable=false, columnDefinition = "nvarchar(500)")
    private String question;

    @Column(name="Answer",nullable = false, columnDefinition = "nvarchar(2000)")
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="Theme_ID", referencedColumnName ="ID")
    private Theme theme;

    public FAQ() {}

    @Transient
    @JsonProperty("theme")
    public String getThemeName() {
        return (theme!=null)?(theme.getName()):(null);
    }

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

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

}
