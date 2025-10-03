package ru.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name="Users")
public class User extends PanacheEntityBase {

    @Id
    @Column(name="MAX_ID",nullable = false)
    private Long id;

    @Column(name="MAX_Name", columnDefinition = "nvarchar(400)")
    private String maxName;

    @ManyToOne()
    @JoinColumn(name="Role_ID", referencedColumnName = "ID",nullable = false)
    private Role role;

    @Transient
    @JsonProperty("role")
    public String getUserRoleName(){
        return role!=null?role.getName():"";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long maxId) {
        this.id = maxId;
    }

    public void setMaxName(String maxName) {
        this.maxName = maxName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
