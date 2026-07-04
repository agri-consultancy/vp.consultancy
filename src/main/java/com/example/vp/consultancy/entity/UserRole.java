package com.example.vp.consultancy.entity;

/**
 * UserRole Enum - Defines available user roles in the system
 * Used for role-based access control (RBAC)
 * @author VP Consultancy Team
 * @version 1.0
 */
public enum UserRole {
    /** Administrator - Full system access */
    ADMIN,

    /** Consultant - Can manage farmers and consulting schedules */
    CONSULTANT,

    /** Farmer - End user receiving consulting services */
    FARMER
}
