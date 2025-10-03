package ru.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name="Themes")
public class Theme extends PanacheEntityBase {

    @Id
    @Column(name="ID", nullable=false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="Name", unique=true, nullable=false, length=500,columnDefinition = "nvarchar(500)")
    private String name;

    public Theme() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
