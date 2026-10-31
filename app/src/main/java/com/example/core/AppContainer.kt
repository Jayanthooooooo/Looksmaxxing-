package com.example.core

import android.content.Context
import com.example.data.local.LumoraDatabase
import com.example.data.local.PhotoVault
import com.example.data.local.datastore.PreferencesDataStore
import com.example.data.remote.FirebaseModule
import com.example.data.repo.AuthRepository
import com.example.data.repo.ConsentRepository
import com.example.data.repo.ProfileRepository

class AppContainer(val context: Context) {
    val dispatchers = AppDispatchers()
    val clock: SystemClock = DefaultSystemClock()
    val database: LumoraDatabase by lazy { LumoraDatabase.getInstance(context) }
    val preferencesDataStore: PreferencesDataStore by lazy { PreferencesDataStore(context) }
    val photoVault: PhotoVault by lazy { PhotoVault(context) }
    val firebaseModule: FirebaseModule by lazy { FirebaseModule(context) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(context, firebaseModule.auth, preferencesDataStore, dispatchers)
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(firebaseModule, preferencesDataStore, dispatchers)
    }

    val consentRepository: ConsentRepository by lazy {
        ConsentRepository(firebaseModule, preferencesDataStore, dispatchers)
    }

    val scanRepository: com.example.data.repo.ScanRepository by lazy {
        com.example.data.repo.ScanRepository(database.scanDao(), database.metricNoiseDao(), photoVault, dispatchers)
    }

    val coachRepository: com.example.data.repo.CoachRepository by lazy {
        com.example.data.repo.CoachRepository(database.chatDao(), preferencesDataStore, dispatchers)
    }


    val habitRepository: com.example.data.repo.HabitRepository by lazy {
        com.example.data.repo.HabitRepository(database.habitDao(), dispatchers)
    }

    val dataExportRepository: com.example.data.repo.DataExportRepository by lazy {
        com.example.data.repo.DataExportRepository(context, database, photoVault, preferencesDataStore, dispatchers)
    }
}


