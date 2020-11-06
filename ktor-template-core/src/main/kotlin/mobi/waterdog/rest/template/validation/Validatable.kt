package mobi.waterdog.rest.template.validation

import mobi.waterdog.rest.template.exception.AppException
import mobi.waterdog.rest.template.exception.ErrorCode
import mobi.waterdog.rest.template.exception.ErrorDefinition
import org.valiktor.Constraint
import org.valiktor.ConstraintViolationException
import org.valiktor.Validator
import org.valiktor.constraints.Between
import org.valiktor.constraints.Equals
import org.valiktor.constraints.Greater
import org.valiktor.constraints.GreaterOrEqual
import org.valiktor.constraints.In
import org.valiktor.constraints.Less
import org.valiktor.constraints.LessOrEqual
import org.valiktor.constraints.NotBetween
import org.valiktor.constraints.NotEquals
import org.valiktor.constraints.NotIn
import org.valiktor.constraints.Size
import org.valiktor.validate

abstract class Validatable<T> {

    protected abstract fun rules(validator: Validator<T>)

    fun applyRules(validator: Validator<T>? = null) {
        validator?.let {
            rules(it)
        } ?: validate(this) {
            @Suppress("UNCHECKED_CAST")
            rules(this as Validator<T>)
        }
    }

    /**
     * All rules defined in rules method will be applied here.
     *
     * @throws AppException if a validation fails
     */
    fun validate() {
        try {
            applyRules()
        } catch (ex: ConstraintViolationException) {
            throw valiktorException2AppException(ex)
        }
    }

    private fun minMaxMap(min: Any?, max: Any?) =
        mapOf("min" to min.toString(), "max" to max.toString())

    private fun valueMap(value: Any?) = mapOf("value" to value.toString())

    private fun violatedConstraint2map(valiktorConstraint: Constraint): Map<String, String> =
        when (valiktorConstraint) {
            is Between<*> -> minMaxMap(valiktorConstraint.start, valiktorConstraint.end)
            is NotBetween<*> -> minMaxMap(valiktorConstraint.start, valiktorConstraint.end)
            is Size -> minMaxMap(valiktorConstraint.min, valiktorConstraint.max)
            is In<*> -> valueMap(valiktorConstraint.values)
            is NotIn<*> -> valueMap(valiktorConstraint.values)
            is GreaterOrEqual<*> -> valueMap(valiktorConstraint.value)
            is Greater<*> -> valueMap(valiktorConstraint.value)
            is LessOrEqual<*> -> valueMap(valiktorConstraint.value)
            is Less<*> -> valueMap(valiktorConstraint.value)
            is Equals<*> -> valueMap(valiktorConstraint.value)
            is NotEquals<*> -> valueMap(valiktorConstraint.value)
            else -> mapOf()
        }

    private fun valiktorException2AppException(valiktorEx: ConstraintViolationException): AppException {
        val validationException = AppException(
            code = ErrorCode.InvalidUserInput,
            title = "Invalid content for class ${this::class.qualifiedName}, check invalid fields in errors"
        )

        validationException.errors.addAll(
            valiktorEx.constraintViolations
                .map {
                    ErrorDefinition(
                        "errors.validation.${it.property}.${it.constraint.name}".toLowerCase(),
                        it.property,
                        violatedConstraint2map(it.constraint)
                    )
                }
                .toList()
        )
        return validationException
    }
}
