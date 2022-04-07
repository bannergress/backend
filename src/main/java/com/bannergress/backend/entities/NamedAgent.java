package com.bannergress.backend.entities;

import com.bannergress.backend.enums.Faction;
import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;

import javax.persistence.*;

import java.util.Set;

/**
 * Represents information about an agent identified by its name.
 */
@Entity
@Table(name = "named_agent")
@Audited
@AuditTable("named_agent_audit")
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class NamedAgent {
    /**
     * Agent name.
     */
    @Id
    @FullTextField
    private String name;

    /**
     * Agent faction.
     */
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private Faction faction;

    /**
     * Missions from the author.
     */
    @OneToMany(mappedBy = "author")
    private Set<Mission> missions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Faction getFaction() {
        return faction;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
    }

    public Set<Mission> getMissions() {
        return missions;
    }

    public void setMissions(Set<Mission> missions) {
        this.missions = missions;
    }
}
