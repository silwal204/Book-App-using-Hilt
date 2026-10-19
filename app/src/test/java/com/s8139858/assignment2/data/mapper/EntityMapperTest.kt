package com.s8139858.assignment2.data.mapper

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonParser
import com.s8139858.assignment2.data.remote.dto.DashboardResponse
import org.junit.Test

class EntityMapperTest {

    private val mapper = EntityMapper()

    private fun json(body: String) = JsonParser.parseString(body).asJsonObject

    @Test
    fun `given json object when mapped then field order matches json order`() {
        val entity = mapper.map(
            json("""{"title":"1984","author":"George Orwell","genre":"Dystopian","publicationYear":1949}"""),
            fallbackIndex = 0,
        )

        assertThat(entity.fields.map { it.label })
            .containsExactly("Title", "Author", "Genre", "Publication Year").inOrder()
        assertThat(entity.fields.map { it.value })
            .containsExactly("1984", "George Orwell", "Dystopian", "1949").inOrder()
    }

    @Test
    fun `given description key when mapped then description is separated from fields`() {
        val entity = mapper.map(
            json("""{"title":"1984","description":"A dystopian novel."}"""),
            fallbackIndex = 0,
        )

        assertThat(entity.description).isEqualTo("A dystopian novel.")
        assertThat(entity.fields.map { it.label }).containsExactly("Title")
    }

    @Test
    fun `given null field value when mapped then placeholder shown`() {
        val entity = mapper.map(json("""{"title":null}"""), fallbackIndex = 0)

        assertThat(entity.fields[0].value).isEqualTo("-")
    }

    @Test
    fun `given missing description when mapped then description is null`() {
        val entity = mapper.map(json("""{"title":"1984"}"""), fallbackIndex = 0)

        assertThat(entity.description).isNull()
    }

    @Test
    fun `given unexpected extra fields when mapped then all are kept`() {
        val entity = mapper.map(
            json("""{"title":"1984","rating":4.9,"inPrint":true}"""),
            fallbackIndex = 0,
        )

        assertThat(entity.fields.map { it.label })
            .containsExactly("Title", "Rating", "In Print").inOrder()
        assertThat(entity.fields.map { it.value })
            .containsExactly("1984", "4.9", "true").inOrder()
    }

    @Test
    fun `given nested object when mapped then it renders as compact json without crashing`() {
        val entity = mapper.map(
            json("""{"title":"1984","publisher":{"name":"Secker","city":"London"}}"""),
            fallbackIndex = 0,
        )

        val publisher = entity.fields[1]
        assertThat(publisher.label).isEqualTo("Publisher")
        assertThat(publisher.value).contains("Secker")
    }

    @Test
    fun `given snake and kebab keys when mapped then labels are humanised`() {
        val entity = mapper.map(
            json("""{"author_name":"Orwell","published-date":"1949-06-08"}"""),
            fallbackIndex = 0,
        )

        assertThat(entity.fields.map { it.label })
            .containsExactly("Author Name", "Published Date").inOrder()
    }

    @Test
    fun `given id key when mapped then used as entity id and excluded from fields`() {
        val entity = mapper.map(json("""{"id":"bk-7","title":"1984"}"""), fallbackIndex = 3)

        assertThat(entity.id).isEqualTo("bk-7")
        assertThat(entity.fields.map { it.label }).containsExactly("Title")
    }

    @Test
    fun `given no id key when mapped then fallback index is the id`() {
        val entity = mapper.map(json("""{"title":"1984"}"""), fallbackIndex = 5)

        assertThat(entity.id).isEqualTo("5")
    }

    @Test
    fun `given dashboard response when mapAll then null tolerant defaults apply`() {
        val entities = mapper.mapAll(DashboardResponse(entities = null, entityTotal = null))

        assertThat(entities).isEmpty()
    }
}
