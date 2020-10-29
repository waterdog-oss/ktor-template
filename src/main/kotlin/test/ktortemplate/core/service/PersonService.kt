package test.ktortemplate.core.service

import test.ktortemplate.core.model.Person
import test.ktortemplate.core.utils.pagination.PageRequest

interface PersonService {
    suspend fun add(person: Person): Person
    suspend fun update(person: Person): Person
    suspend fun getById(personId: Int): Person?
    suspend fun deleteById(personId: Int)
    suspend fun count(pageRequest: PageRequest): Int
    suspend fun list(pageRequest: PageRequest): List<Person>
}
