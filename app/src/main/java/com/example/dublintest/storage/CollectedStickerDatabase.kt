package com.example.dublintest.storage

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Entity(tableName = "collected_stickers")
data class CollectedSticker(
    @PrimaryKey val label: String,
    val collectedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val points: Int = 0
)

@Dao
interface CollectedStickerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sticker: CollectedSticker)

    @Query("SELECT * FROM collected_stickers")
    suspend fun getAll(): List<CollectedSticker>

    @Query("DELETE FROM collected_stickers")
    suspend fun clear()
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getStats(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: UserStats)

    @Query("UPDATE user_stats SET points = points + :amount WHERE id = 1")
    suspend fun addPoints(amount: Int)

    @Query("DELETE FROM user_stats")
    suspend fun clear()


}


@Database(entities = [CollectedSticker::class, UserStats::class], version = 2)
abstract class AnimalDatabase : RoomDatabase() {
    abstract fun stickerDao(): CollectedStickerDao
    abstract fun userStatsDao(): UserStatsDao

    companion object {
        @Volatile private var INSTANCE: AnimalDatabase? = null

        fun getInstance(context: Context): AnimalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnimalDatabase::class.java,
                    "stickers.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object StickerRepository {
    private lateinit var db: AnimalDatabase

    fun init(context: Context) {
        db = AnimalDatabase.getInstance(context)

        // Ensure points table is initialized
        CoroutineScope(Dispatchers.IO).launch {
            val existing = db.userStatsDao().getStats()
            if (existing == null) {
                db.userStatsDao().insert(UserStats()) // Starts at 0 points
            }
        }
    }


    suspend fun collect(label: String) {
        withContext(Dispatchers.IO) {
            db.stickerDao().insert(CollectedSticker(label))
        }
    }

    suspend fun getCollected(): List<CollectedSticker> {
        return withContext(Dispatchers.IO) {
            db.stickerDao().getAll()
        }
    }

    suspend fun addPoints(amount: Int) {
        withContext(Dispatchers.IO) {
            db.userStatsDao().addPoints(amount)
        }
    }

    suspend fun getPoints(): Int {
        return withContext(Dispatchers.IO) {
            db.userStatsDao().getStats()?.points ?: 0
        }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) {
            db.stickerDao().clear()
            db.userStatsDao().clear()
        }
    }
}
