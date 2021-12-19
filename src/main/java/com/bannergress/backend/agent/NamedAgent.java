package com.bannergress.backend.agent;

import com.bannergress.backend.mission.Mission;
import com.bannergress.backend.utils.PojoBuilder;
import jakarta.persistence.*;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.type.SqlTypes;

import java.util.Set;

/**
 * Represents information about an agent identified by its name.
 */
@Entity
@Table(name = "named_agent")
@Audited
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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Faction faction;

    /**
     * Missions of the agent.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author")
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
