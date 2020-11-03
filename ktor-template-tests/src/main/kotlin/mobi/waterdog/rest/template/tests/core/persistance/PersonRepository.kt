package mobi.waterdog.rest.template.tests.core.persistance

import mobi.waterdog.rest.template.pagination.PageRequest
import mobi.waterdog.rest.template.tests.core.model.Person

interface PersonRepository {
    fun save(person: Person): Person
    fun update(person: Person): Person
    fun getById(id: Int): Person?
    fun delete(id: Int)
    fun count(pageRequest: PageRequest = PageRequest()): Int
    fun list(pageRequest: PageRequest = PageRequest()): List<Person>
}
