package com.example.thehive

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest : DaoTestBase() {

    @Test fun insertAndFindByEmail() = runBlocking {
        db.userDao().insert(user("x@test.com"))
        assertEquals("x@test.com", db.userDao().findByEmail("x@test.com")?.email)
    }

    @Test fun findById() = runBlocking {
        val id = db.userDao().insert(user())
        assertEquals(id, db.userDao().findById(id)?.id)
    }

    @Test fun findByEmailReturnsNullWhenMissing() = runBlocking {
        assertNull(db.userDao().findByEmail("nobody@test.com"))
    }

    @Test fun update() = runBlocking {
        val id = db.userDao().insert(user())
        val saved = db.userDao().findById(id)!!
        db.userDao().update(saved.copy(fullName = "New Name"))
        assertEquals("New Name", db.userDao().findById(id)?.fullName)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun duplicateEmailRejected() = runBlocking<Unit> {
        db.userDao().insert(user("dup@test.com"))
        db.userDao().insert(user("dup@test.com"))
    }
}
