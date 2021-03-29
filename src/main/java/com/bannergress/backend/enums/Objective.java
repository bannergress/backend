package com.bannergress.backend.enums;

/** Represents the action that has to be performed at a location. */
public enum Objective {
	/** Hack this portal. */
	hack,
	/** Capture or upgrade portal. */
	captureOrUpgrade,
	/** Create link from portal. */
	createLink,
	/** Create field from portal. */
	createField,
	/** Install mod on this portal. */
	installMod,
	/** Take a photo of this portal. */
	takePhoto,
	/** View this field trip waypoint. */
	viewWaypoint,
	/** Enter the passphrase. */
	enterPassphrase
}
