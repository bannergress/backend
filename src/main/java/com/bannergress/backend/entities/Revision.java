package com.bannergress.backend.entities;

import com.bannergress.backend.security.CustomRevisionListener;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Revision information.
 */
@Entity
@Table(name = "revision")
@RevisionEntity(CustomRevisionListener.class)
public class Revision extends DefaultRevisionEntity {
    private static final long serialVersionUID = 1L;

    @Column(name = "userid", nullable = true)
    private String userid;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
