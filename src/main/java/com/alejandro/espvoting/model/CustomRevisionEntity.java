package com.alejandro.espvoting.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import com.alejandro.espvoting.config.CustomRevisionListener;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity to store revision information for Hibernate Envers.
 */
@Entity
@Table(name = "revinfo")
@RevisionEntity(CustomRevisionListener.class)
@Getter
@Setter
public class CustomRevisionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private int id;

    @RevisionTimestamp
    private long timestamp;

    /**
     * Get the date of this revision.
     * @return the date of this revision
     */
    @Transient
    public Date getRevisionDate() {
        return new Date(timestamp);
    }
}
