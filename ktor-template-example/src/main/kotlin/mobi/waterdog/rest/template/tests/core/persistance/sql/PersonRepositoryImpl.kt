package mobi.waterdog.rest.template.tests.core.persistance.sql

import mobi.waterdog.rest.template.exception.AppException
import mobi.waterdog.rest.template.exception.ErrorCode
import mobi.waterdog.rest.template.pagination.PageRequest
import mobi.waterdog.rest.template.pagination.SortField
import mobi.waterdog.rest.template.tests.core.model.Person
import mobi.waterdog.rest.template.tests.core.model.Persons
import mobi.waterdog.rest.template.tests.core.persistance.PersonRepository
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SortOrder

class PersonRepositoryImpl : PersonRepository {

    override fun save(person: Person): Person {
        return Person.new { person }
    }

    override fun update(person: Person): Person {
        val dbPerson = getById(person.id.value)!!
        dbPerson.name = person.name
        dbPerson.birthday = person.birthday
        return dbPerson
    }

    override fun getById(id: Int): Person? {
        return Person.findById(id)
    }

    override fun delete(id: Int) {
        getById(id)!!.delete()
    }

    override fun count(pageRequest: PageRequest): Int {
        return Person
            .all()
            .limit(pageRequest.limit, pageRequest.offset.toLong())
            .count()
            .toInt()
    }

    override fun list(pageRequest: PageRequest): List<Person> {
        val sorts = pageRequest.sort
            .map { sortToColumn(it) }
            .toTypedArray()

        return Person
            .all()
            .limit(pageRequest.limit, pageRequest.offset.toLong())
            .orderBy(*sorts)
            .toList()
    }

    private fun sortToColumn(sort: SortField): Pair<Column<*>, SortOrder> {
        // TODO we should use reflection here
        val field = when (sort.field) {
            "name" -> Persons.name
            "birthday" -> Persons.birthday
            else -> throw AppException(
                ErrorCode.NotImplemented,
                "Sort by column '${sort.field}' in Person table not " +
                    "implemented."
            )
        }
        return field to SortOrder.valueOf(sort.order.name.toUpperCase())
    }
}
