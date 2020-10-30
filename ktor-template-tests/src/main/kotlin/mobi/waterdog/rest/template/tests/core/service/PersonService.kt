package mobi.waterdog.rest.template.tests.core.service

import mobi.waterdog.rest.template.tests.core.model.Person
import mobi.waterdog.rest.template.pagination.PageRequest

interface PersonService {
    suspend fun add(person: Person): Person
    suspend fun update(person: Person): Person
    suspend fun getById(personId: Int): Person?
    suspend fun deleteById(personId: Int)
    suspend fun count(pageRequest: PageRequest): Int
    suspend fun list(pageRequest: PageRequest): List<Person>
}
