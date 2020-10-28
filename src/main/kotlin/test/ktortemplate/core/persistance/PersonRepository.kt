package test.ktortemplate.core.persistance

import test.ktortemplate.core.model.Person
import test.ktortemplate.core.utils.pagination.PageRequest

interface PersonRepository {
    fun save(person: Person): Person
    fun update(person: Person): Person
    fun getById(id: Int): Person?
    fun delete(id: Int)
    fun count(pageRequest: PageRequest = PageRequest()): Int
    fun list(pageRequest: PageRequest = PageRequest()): List<Person>
}
