package com.example.locationtracker.domain.enums

/**
 * Represents the online presence status of a user
 */
enum class PresenceStatus {
    /**
     * User is actively using the app (last activity < 5 minutes)
     */
    ONLINE,

    /**
     * User was recently active (last activity 5-30 minutes)
     */
    AWAY,

    /**
     * User hasn't been active for more than 30 minutes
     */
    OFFLINE;

    companion object {
        /**
         * Determine presence status based on last seen timestamp
         * @param lastSeenMillis Timestamp in milliseconds
         * @return PresenceStatus based on time elapsed
         */
        fun fromLastSeen(lastSeenMillis: Long): PresenceStatus {
            val now = System.currentTimeMillis()
            val minutesElapsed = (now - lastSeenMillis) / (1000 * 60)

            return when {
                minutesElapsed < 5 -> ONLINE
                minutesElapsed < 30 -> AWAY
                else -> OFFLINE
            }
        }
    }
}