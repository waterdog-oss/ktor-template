package mobi.waterdog.rest.template.core.persistance

import mobi.waterdog.rest.template.core.model.Person
import mobi.waterdog.rest.template.core.utils.pagination.PageRequest

interface PersonRepository {
    fun save(person: Person): Person
    fun update(person: Person): Person
    fun getById(id: Int): Person?
    fun delete(id: Int)
    fun count(pageRequest: PageRequest = PageRequest()): Int
    fun list(pageRequest: PageRequest = PageRequest()): List<Person>
}
