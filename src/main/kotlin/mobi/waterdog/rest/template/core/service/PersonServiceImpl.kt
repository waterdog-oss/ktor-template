package mobi.waterdog.rest.template.core.service

import org.koin.core.KoinComponent
import org.koin.core.inject
import mobi.waterdog.rest.template.conf.database.DatabaseConnection
import mobi.waterdog.rest.template.core.model.Person
import mobi.waterdog.rest.template.core.persistance.PersonRepository
import mobi.waterdog.rest.template.core.utils.pagination.PageRequest

class PersonServiceImpl : KoinComponent, PersonService {

    private val personRepository: PersonRepository by inject()
    private val dbc: DatabaseConnection by inject()

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
