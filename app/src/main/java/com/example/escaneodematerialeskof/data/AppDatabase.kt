package com.example.escaneodematerialeskof.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The Room database for this app.
 */
@Database(entities = [ScanHistory::class, com.example.escaneodematerialeskof.model.Pallet::class, com.example.escaneodematerialeskof.model.Rumba::class, com.example.escaneodematerialeskof.model.Restos::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun scanHistoryDao(): ScanHistoryDao
	abstract fun palletDao(): PalletDao
	abstract fun rumbaDao(): RumbaDao
	abstract fun restosDao(): RestosDao
	
	companion object {
		@Volatile
		private var INSTANCE: AppDatabase? = null
		
		fun getDatabase(context: Context): AppDatabase {
			return INSTANCE ?: synchronized(this) {
				val instance = Room.databaseBuilder(
					context.applicationContext,
					AppDatabase::class.java,
					"inventory_database"
				)
					.fallbackToDestructiveMigration()
					.build()
				INSTANCE = instance
				instance
			}
		}
	}
}

/**
 * Type converters for Room database.
 */
@androidx.room.TypeConverters
class Converters {
	@androidx.room.TypeConverter
	fun fromTimestamp(value: Long?): java.util.Date? {
		return value?.let { java.util.Date(it) }
	}
	
	@androidx.room.TypeConverter
	fun dateToTimestamp(date: java.util.Date?): Long? {
		return date?.time
	}
}