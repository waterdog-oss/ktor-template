package test.ktortemplate.core.service

import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.conf.database.DatabaseConnection
import test.ktortemplate.core.model.Person
import test.ktortemplate.core.persistance.PersonRepository
import test.ktortemplate.core.utils.pagination.PageRequest

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
