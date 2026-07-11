package com.restro.dto;

/**
 * Which dashboard a staff login can access. A future full RBAC module
 * (see Future Enhancements) can migrate this to a role_id FK / permissions
 * table without breaking existing staff rows - KITCHEN/COUNTER would just
 * become the two seeded default roles.
 */
public enum StaffRole {
    KITCHEN,
    COUNTER
}
