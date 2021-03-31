package com.bannergress.backend.entities;

import com.bannergress.backend.enums.Objective;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import javax.persistence.*;

/**
 * Represents a step that has to be performed in order to complete a mission.
 */
@Entity
@Table(name = "mission_step")
@Audited
@AuditTable("mission_step_audit")
public class MissionStep {
    /**
     * Internal ID without further meaning.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mission")
    private Mission mission;

    /**
     * The POI on which the action has to be performed.
     */
    @ManyToOne(optional = true)
    @JoinColumn(name = "poi")
    private POI poi;

    /**
     * The action that has to be performed.
     */
    @Column(name = "objective", nullable = true)
    @Enumerated(EnumType.STRING)
    private Objective objective;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public POI getPoi() {
        return poi;
    }

    public void setPoi(POI poi) {
        this.poi = poi;
    }

    public Objective getObjective() {
        return objective;
    }

    public void setObjective(Objective objective) {
        this.objective = objective;
    }
}
