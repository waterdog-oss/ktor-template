package mobi.waterdog.rest.template.core.utils.exception

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

/**
 * Intended to be the result of an erroneous http call.
 */
@Serializable
data class ErrorDTO(
    val httpStatusCode: Int,
    val messageCode: String? = null,
    val title: String? = null,
    val errors: List<ErrorDefinition> = listOf()
) {
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID()

    override fun toString(): String {
        return "ErrorDTO(id=$id, httpStatusCode=$httpStatusCode, messageCode=$messageCode, title=$title, errors=$errors)"
    }
}

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}

@Serializable
data class ErrorDefinition(val errorCode: String, val field: String?, val args: Map<String, String>) {
    override fun toString(): String {
        return "ErrorDefinition(errorCode='$errorCode', field=$field, args=$args)"
    }
}
