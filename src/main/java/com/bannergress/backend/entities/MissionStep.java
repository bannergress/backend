package com.bannergress.backend.entities;

import com.bannergress.backend.enums.Objective;
import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import javax.persistence.*;

import java.util.UUID;

/**
 * Represents a step that has to be performed in order to complete a mission.
 */
@Entity
@Table(name = "mission_step")
@Audited
@AuditTable("mission_step_audit")
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class MissionStep {
    /**
     * Internal UUID without further meaning.
     */
    @Id
    @Column(name = "uuid", columnDefinition = "uuid")
    @GeneratedValue
    private UUID uuid;

    @ManyToOne(optional = true)
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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
