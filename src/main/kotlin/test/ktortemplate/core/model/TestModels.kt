package test.ktortemplate.core.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import java.time.Instant

@JsonClass(generateAdapter = true)
data class TestInstantSerialization(val date: Instant)

class InstantAdapter {
    @ToJson
    fun toJson(instant: Instant): Long {
        return instant.toEpochMilli()
    }

    @FromJson
    fun fromJson(instant: Long): Instant {
        return Instant.ofEpochMilli(instant)
    }
}

@JsonClass(generateAdapter = true)
class TestSealedClass(val exp: Expr)

sealed class Expr

@JsonClass(generateAdapter = true)
class Const(val number: Double) : Expr()

@JsonClass(generateAdapter = true)
data class Sum(val e1: Expr, val e2: Expr) : Expr()

@JsonClass(generateAdapter = true)
class NotANumber : Expr()
