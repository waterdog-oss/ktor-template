package mobi.waterdog.rest.template.validation

import org.valiktor.Constraint
import org.valiktor.Validator

/**
 * Represents a constraint that validate if the value is a valid phone number
 */
object PhoneNumber : Constraint

/**
 * Validates if the [String] property value is a valid phone number
 *
 * @receiver the property to be validated
 * @return the same receiver property
 */
fun <E> Validator<E>.Property<String?>.isPhoneNumber(): Validator<E>.Property<String?> =
    this.validate(PhoneNumber) {
        it == null || it.matches(Regex("^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*\$"))
    }
