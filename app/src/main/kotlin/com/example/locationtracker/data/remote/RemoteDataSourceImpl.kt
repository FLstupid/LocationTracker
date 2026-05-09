package com.example.locationtracker.data.remote

import com.example.locationtracker.data.datasource.RemoteDataSource
import com.example.locationtracker.domain.model.Family
import com.example.locationtracker.domain.model.FriendRequest
import com.example.locationtracker.domain.model.LiveLocation
import com.example.locationtracker.domain.model.TrackedLocation
import com.example.locationtracker.domain.model.User
import com.example.locationtracker.domain.model.UserPresence
import com.example.locationtracker.domain.enums.PresenceStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.util.Date

class RemoteDataSourceImpl(private val auth: FirebaseAuth, private val firestore: FirebaseFirestore) :
    RemoteDataSource {

    private val currentUserUid: String? get() = auth.currentUser?.uid

    override fun getCurrentUserId(): String {
        return currentUserUid ?: ""
    }

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val registration = currentUserUid?.let { uid ->
            firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toObject(User::class.java))
                }
        } ?: run { trySend(null) ; null }

        awaitClose { registration?.remove() }
    }

    override suspend fun getUsers(uids: List<String>): Flow<List<User>> {
        val filteredUids = uids.filter { it.isNotBlank() }
        if (filteredUids.isEmpty()) {
            return flowOf(emptyList())
        }
        return callbackFlow {
            val registration = firestore.collection("users")
                .whereIn(FieldPath.documentId(), filteredUids)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toObjects(User::class.java) ?: emptyList())
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun updateDisplayName(displayName: String) {
        currentUserUid?.let { uid ->
            val userUpdate = mapOf("displayName" to displayName)
            firestore.collection("users").document(uid).set(userUpdate, SetOptions.merge()).await()
        }
    }

    override suspend fun toggleFriendSharing(friendUid: String, enable: Boolean) {
        currentUserUid?.let { uid ->
            val update = if (enable) {
                mapOf("sharingWithFriends" to FieldValue.arrayUnion(friendUid))
            } else {
                mapOf("sharingWithFriends" to FieldValue.arrayRemove(friendUid))
            }
            firestore.collection("users").document(uid).set(update, SetOptions.merge()).await()
        }
    }

    override suspend fun toggleCircleSharing(familyId: String, enable: Boolean) {
        currentUserUid?.let { uid ->
            val update = if (enable) {
                mapOf("sharingWithCircles" to FieldValue.arrayUnion(familyId))
            } else {
                mapOf("sharingWithCircles" to FieldValue.arrayRemove(familyId))
            }
            firestore.collection("users").document(uid).set(update, SetOptions.merge()).await()
        }
    }

    override suspend fun toggleMasterSharing(enable: Boolean) {
        currentUserUid?.let { uid ->
            val update = mapOf("isSharingLocation" to enable)
            firestore.collection("users").document(uid).set(update, SetOptions.merge()).await()
        }
    }


    override fun getFriends(): Flow<List<User>> = callbackFlow {
        var innerRegistration: ListenerRegistration? = null
        val outerRegistration = currentUserUid?.let { uid ->
            firestore.collection("users").document(uid)
                .addSnapshotListener { userSnapshot, e ->
                    if (e != null) {
                        close(e)
                        return@addSnapshotListener
                    }
                    innerRegistration?.remove()
                    val friendUids =
                        userSnapshot?.toObject(User::class.java)?.friends ?: emptyList()

                    if (friendUids.isNotEmpty()) {
                        innerRegistration = firestore.collection("users")
                            .whereIn(FieldPath.documentId(), friendUids)
                            .addSnapshotListener { friendsSnapshot, friendsE ->
                                if (friendsE != null) {
                                    close(friendsE)
                                    return@addSnapshotListener
                                }
                                trySend(friendsSnapshot?.toObjects(User::class.java) ?: emptyList())
                            }
                    } else {
                        trySend(emptyList())
                    }
                }
        }
        awaitClose {
            innerRegistration?.remove()
            outerRegistration?.remove()
        }
    }

    override fun getFamily(): Flow<List<Family>> = callbackFlow {
        val registration = currentUserUid?.let { uid ->
            // FIXED: Changed "familyCircles" to "family" to match createFamily and getFamilyDetail
            firestore.collection("family")
                .whereArrayContains("members", uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toObjects(Family::class.java) ?: emptyList())
                }
        }
        awaitClose { registration?.remove() }
    }

    override fun getFamilyDetail(familyId: String): Flow<Family> {
        return callbackFlow {
            val registration = firestore.collection("family")
                .document(familyId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                        return@addSnapshotListener
                    }
                    val family = snapshot?.toObject(Family::class.java)

                    if (family != null) {
                        trySend(family)
                    }
                }

            awaitClose { registration.remove() }
        }
    }

    override suspend fun searchUsers(query: String): List<User> {
        return firestore.collection("users")
            .whereEqualTo("phone", query)
            .get()
            .await()
            .toObjects(User::class.java)
            .filter { it.uid != currentUserUid }
    }

    override suspend fun sendFriendRequest(toUid: String, fromPhone: String) {
        currentUserUid?.let { fromUid ->
            // Fetch current user details first to get the name
            val currentUserDoc = firestore.collection("users").document(fromUid).get().await()
            val currentUser = currentUserDoc.toObject(User::class.java)
            val fromName = currentUser?.displayName ?: "Unknown User"

            val requestId = "${fromUid}_${toUid}"
            val friendRequest = FriendRequest(
                id = requestId,
                fromUid = fromUid,
                fromName = fromName,
                toUid = toUid,
                fromPhone = fromPhone,
                status = "pending"
            )
            firestore.collection("friendRequests").document(requestId).set(friendRequest).await()
        }
    }

    override suspend fun acceptFriendRequest(request: FriendRequest) {
        currentUserUid?.let { toUid ->
            firestore.runTransaction { transaction ->
                val fromUserRef = firestore.collection("users").document(request.fromUid)
                val toUserRef = firestore.collection("users").document(toUid)
                val friendRequestRef = firestore.collection("friendRequests").document("${request.fromUid}_${toUid}")

                transaction.update(friendRequestRef, "status", "accepted")
                transaction.update(fromUserRef, "friends", FieldValue.arrayUnion(toUid))
                transaction.update(toUserRef, "friends", FieldValue.arrayUnion(request.fromUid))

                null
            }.await()
        }
    }

    override suspend fun rejectFriendRequest(request: FriendRequest) {
        currentUserUid?.let { toUid ->
            firestore.collection("friendRequests").document("${request.fromUid}_${toUid}")
                .update("status", "rejected").await()
        }
    }

    override suspend fun createFamily(name: String): Boolean {
        return currentUserUid?.let { uid ->
            try {
                val newFamily = Family(id = firestore.collection("family").document().id, name = name, members = listOf(uid), ownerUid = uid)
                firestore.collection("family").document(newFamily.id).set(newFamily).await()
                true
            } catch (_: Exception) {
                false
            }
        } ?: false
    }

    override fun getIncomingFriendRequests(): Flow<List<FriendRequest>> = callbackFlow {
        val registration = currentUserUid?.let { uid ->
            firestore.collection("friendRequests")
                .whereEqualTo("toUid", uid)
                .whereEqualTo("status", "pending")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toObjects(FriendRequest::class.java) ?: emptyList())
                }
        }
        awaitClose { registration?.remove() }
    }

    override suspend fun signUp(name: String, email: String, password: String, phone: String): Boolean {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val newUser = User(
                    uid = firebaseUser.uid,
                    email = email,
                    displayName = name,
                    phone = phone
                )
                // Ensure we save the user object, not just update it
                firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            print("RemoteDataSourceImpl signUp: ${e.message}")
            false
        }
    }

    override suspend fun signIn(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun updateLiveLocation(latitude: Double, longitude: Double) {
        currentUserUid?.let { uid ->
            if (uid.isNotBlank()) {
                val liveLocation = LiveLocation(uid, latitude, longitude)

                firestore.collection("liveLocations").document(uid).set(liveLocation).await()
                addTrackedLocation(uid, latitude, longitude)
            }
        }
    }

    // Location methods
    override fun getLiveLocations(sharedUserIds: List<String>): Flow<Map<String, LiveLocation>> = callbackFlow {
        val liveLocationsMap = mutableMapOf<String, LiveLocation>()
        val registrations = mutableListOf<ListenerRegistration>()

        val filteredUserIds = sharedUserIds.filter { it.isNotBlank() }

        if (filteredUserIds.isEmpty()) {
            trySend(emptyMap())
            awaitClose {}
            return@callbackFlow
        }

        for (userId in filteredUserIds) {
            val registration = firestore.collection("liveLocations").document(userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val liveLoc = snapshot.toObject(LiveLocation::class.java)
                        if (liveLoc != null) {
                            liveLocationsMap[userId] = liveLoc
                            trySend(liveLocationsMap.toMap())
                        }
                    } else {
                        liveLocationsMap.remove(userId)
                        trySend(liveLocationsMap.toMap())
                    }
                }
            registrations.add(registration)
        }

        awaitClose { registrations.forEach { it.remove() } }
    }

    override fun getLocationHistory(startDate: Date?, endDate: Date?): Flow<List<TrackedLocation>> = callbackFlow {
        val registration = currentUserUid?.let { uid ->
            var query = firestore.collection("locations")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp")

            if (startDate != null) {
                query = query.whereGreaterThanOrEqualTo("timestamp", startDate)
            }
            if (endDate != null) {
                query = query.whereLessThanOrEqualTo("timestamp", endDate)
            }

            query.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(TrackedLocation::class.java)?.sortedBy { it.timestamp } ?: emptyList())
            }
        }
        awaitClose { registration?.remove() }
    }

    override fun getLocationHistoryForUser(userId: String, startDate: Date?, endDate: Date?): Flow<List<TrackedLocation>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }

        var query = firestore.collection("locations")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp")

        if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("timestamp", startDate)
        }
        if (endDate != null) {
            query = query.whereLessThanOrEqualTo("timestamp", endDate)
        }

        val registration = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(TrackedLocation::class.java)?.sortedBy { it.timestamp } ?: emptyList())
        }
        awaitClose { registration.remove() }
    }

    override suspend fun addTrackedLocation(userId: String, latitude: Double, longitude: Double) {
        val location = TrackedLocation(
            userId = userId,
            latitude = latitude,
            longitude = longitude,
            timestamp = Date()
        )
        firestore.collection("locations").add(location).await()
    }

    override fun getUserPresence(userId: String): Flow<UserPresence?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            awaitClose {}
            return@callbackFlow
        }

        val registration = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val presenceData = snapshot?.get("presence") as? Map<*, *>
                val presence = presenceData?.let {
                    try {
                        UserPresence(
                            userId = userId,
                            status = PresenceStatus.valueOf(it["status"] as? String ?: "OFFLINE"),
                            lastSeen = it["lastSeen"] as? Long ?: 0L,
                            batteryLevel = (it["batteryLevel"] as? Long)?.toInt(),
                            isSharing = it["isSharing"] as? Boolean ?: false,
                            currentPlace = it["currentPlace"] as? String
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                trySend(presence)
            }

        awaitClose { registration.remove() }
    }

    override fun getUsersPresence(userIds: List<String>): Flow<Map<String, UserPresence>> = callbackFlow {
        if (userIds.isEmpty()) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("users")
            .whereIn("uid", userIds)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val presenceMap = mutableMapOf<String, UserPresence>()
                snapshot?.documents?.forEach { doc ->
                    val userId = doc.id
                    val presenceData = doc.get("presence") as? Map<*, *>
                    presenceData?.let {
                        try {
                            val presence = UserPresence(
                                userId = userId,
                                status = PresenceStatus.valueOf(it["status"] as? String ?: "OFFLINE"),
                                lastSeen = it["lastSeen"] as? Long ?: 0L,
                                batteryLevel = (it["batteryLevel"] as? Long)?.toInt(),
                                isSharing = it["isSharing"] as? Boolean ?: false,
                                currentPlace = it["currentPlace"] as? String
                            )
                            presenceMap[userId] = presence
                        } catch (_: Exception) {
                            // Skip invalid presence data
                        }
                    }
                }
                trySend(presenceMap)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun updatePresence(presence: UserPresence) {
        val userId = presence.userId.ifEmpty { currentUserUid } ?: return

        val presenceData = hashMapOf(
            "status" to presence.status.name,
            "lastSeen" to presence.lastSeen,
            "batteryLevel" to presence.batteryLevel,
            "isSharing" to presence.isSharing,
            "currentPlace" to presence.currentPlace
        )

        firestore.collection("users")
            .document(userId)
            .set(mapOf("presence" to presenceData), SetOptions.merge())
            .await()
    }

    override suspend fun updatePhotoUrl(photoUrl: String) {
        val userId = currentUserUid ?: return

        firestore.collection("users")
            .document(userId)
            .update("photoUrl", photoUrl)
            .await()
    }

    override suspend fun updatePhone(phone: String) {
        val userId = currentUserUid ?: return

        firestore.collection("users")
            .document(userId)
            .update("phone", phone)
            .await()
    }

    override suspend fun updateFcmToken(token: String) {
        val userId = currentUserUid ?: return

        firestore.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .await()
    }

    override suspend fun getFcmToken(userId: String): String? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            document.getString("fcmToken")
        } catch (_: Exception) {
            null
        }
    }
}
