package com.s8139858.assignment2.domain.model

/** Dashboard payload in domain terms: the mapped [entities] plus the server-reported [entityTotal]. */
data class DashboardData(
    val entities: List<Entity>,
    val entityTotal: Int,
)
