package mobi.waterdog.rest.template.tests.core.service

import mobi.waterdog.rest.template.database.DatabaseConnection
import mobi.waterdog.rest.template.pagination.PageRequest
import mobi.waterdog.rest.template.tests.core.model.Person
import mobi.waterdog.rest.template.tests.core.persistance.PersonRepository

class PersonServiceImpl(
    private val personRepository: PersonRepository,
    private val dbc: DatabaseConnection
) : PersonService {
    override suspend fun add(person: Person): Person {
        return dbc.suspendedQuery { personRepository.save(person) }
    }

    override suspend fun update(person: Person): Person {
        return dbc.suspendedQuery { personRepository.update(person) }
    }

    override suspend fun getById(personId: Int): Person? {
        return dbc.suspendedQuery { personRepository.getById(personId) }
    }

    override suspend fun deleteById(personId: Int) {
        dbc.suspendedQuery { personRepository.delete(personId) }
    }

    override suspend fun count(pageRequest: PageRequest): Int {
        return dbc.suspendedQuery { personRepository.count(pageRequest) }
    }

    override suspend fun list(pageRequest: PageRequest): List<Person> {
        return dbc.suspendedQuery { personRepository.list(pageRequest) }
    }
}
