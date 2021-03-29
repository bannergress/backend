package com.bannergress.backend.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import com.bannergress.backend.enums.Faction;

/** Represents information about an agent identified by its name. */
@Entity
@Table(name = "named_agent")
@Audited
@AuditTable("named_agent_audit")
public class NamedAgent {
	/** Agent name. */
	@Id
	private String name;

	/** Agent faction. */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Faction faction;

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
}
