package test.ktortemplate.core.utils.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import test.ktortemplate.core.model.Const
import test.ktortemplate.core.model.Expr
import test.ktortemplate.core.model.InstantAdapter
import test.ktortemplate.core.model.NotANumber
import test.ktortemplate.core.model.Sum

object JsonSettings {

    val mapper: Moshi = Moshi.Builder()
        .add(InstantAdapter())
        .add(
            PolymorphicJsonAdapterFactory.of(Expr::class.java, "exp")
                .withSubtype(Const::class.java, "const")
                .withSubtype(Sum::class.java, "sum")
                .withSubtype(NotANumber::class.java, "notanumber")
        )
        .addLast(KotlinJsonAdapterFactory())
        .build()

    inline fun <reified T> fromJson(json: String?): T {
        val jsonAdapter: JsonAdapter<T> = mapper.adapter(T::class.java)
        return jsonAdapter.fromJson(json!!)!!
    }

    inline fun <reified T> toJson(value: T?): String {
        val jsonAdapter: JsonAdapter<T> = mapper.adapter(T::class.java)
        return jsonAdapter.toJson(value)
    }

    inline fun <reified T> fromJson(json: String?, customAdapter: JsonAdapter<T>): T {
        return customAdapter.fromJson(json!!)!!
    }

    fun <T> toJson(value: T?, customAdapter: JsonAdapter<T>): String {
        return customAdapter.toJson(value)
    }
}
