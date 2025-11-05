package com.vladmihalcea.spring.demo.domain.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.NaturalId;

import java.util.Objects;

/**
 * @author Vlad Mihalcea
 */
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    private Long id;

    @NaturalId
    private String name;

    public Long getId() {
        return id;
    }

    public Tag setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Tag setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tag tag)) return false;
        return Objects.equals(getName(), tag.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }
}
