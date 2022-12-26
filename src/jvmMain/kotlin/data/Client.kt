package data

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Projections
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import data.entity.CollectionsEntity
import data.entity.DatabaseEntity
import data.entity.DocumentEntity
import kotlinx.coroutines.reactive.awaitFirst
import org.bson.UuidRepresentation
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.reactivestreams.*

enum class Operator {
  NOT,
  NOR,
  AND,
  OR;

  companion object {
    fun toList(): List<Operator> {
      return listOf(
        NOT,
        NOR,
        AND,
        OR
      )
    }
  }
}

enum class Comparison(val string: String) {
  Eq("=="),
  Gt(">"),
  Gte(">="),
  Lt("<"),
  Lte("<="),
  Ne("!="),
  In("IN"),
  Nin("NIN");

  companion object {
    fun toList(): List<Comparison> {
      return listOf(Eq, Gt, Gte, Lt, Lte, Ne, In, Nin)
    }
  }
}

data class ConditionEntity(
  val condition: String = "",
  val isString: Boolean = true
) {
  var bsonCondition: Bson = empty()
    private set

  suspend fun update(): ConditionEntity {
    val comparison = Comparison.toList()
    comparison.forEach {
      val list = condition.split(it.string).toList()
      if (list.size == 2) {
        val value = if (isString) list[1] else list[1].toDoubleOrNull()
        when (it) {
          Comparison.Eq -> {
            bsonCondition = eq(list[0], value)
          }

          Comparison.Gt -> {
            bsonCondition = gt(list[0], value)
          }

          Comparison.Gte -> {
            bsonCondition = gte(list[0], value)
          }

          Comparison.Lt -> {
            bsonCondition = lt(list[0], value)
          }

          Comparison.Lte -> {
            bsonCondition = lte(list[0], value)
          }

          Comparison.Ne -> {
            bsonCondition = ne(list[0], value)
          }

          Comparison.In -> {
            bsonCondition = `in`(list[0], value)
          }

          Comparison.Nin -> {
            bsonCondition = nin(list[0], value)
          }
        }
      }
    }
    return this
  }
}

class Client {
  private val uriConnect: String = "mongodb://localhost:27017"
  private val mongoClient: MongoClient = MongoClients.create(
    MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD).applyConnectionString(
      ConnectionString(uriConnect)
    ).build()
  )
  private val gson = GsonBuilder().setPrettyPrinting().create()

  suspend fun getDataBases(): List<DatabaseEntity> {
    val result: MutableList<DatabaseEntity> = mutableListOf()
    mongoClient.listDatabases().toList()
      .forEach {
        println(it.toJson())
        result.add(gson.fromJson(it.toJson(), DatabaseEntity::class.java))
      }
    return result
  }

  suspend fun getCollections(databaseEntity: DatabaseEntity): List<CollectionsEntity> {
    val result: MutableList<CollectionsEntity> = mutableListOf()
    mongoClient.getDatabase(databaseEntity.name).listCollections().toList().forEach {
      println(it.toJson())
      result.add(gson.fromJson(it.toJson(), CollectionsEntity::class.java))
    }
    return result
  }


  suspend fun getDocuments(
    collectionName: String,
    databaseName: String,
    condition: Bson,
    projection: List<String>?
  ): List<DocumentEntity> {
    val result = mutableListOf<DocumentEntity>()

    mongoClient.getDatabase(databaseName).getCollection(collectionName).aggregate(
      listOf(
        Aggregates.project(
          if (projection != null) Projections.include(projection) else Projections.exclude(empty().toString())
        ),
        Aggregates.match(condition),
      )
    )
      .toList().forEach {
        result.add(
          DocumentEntity(
            _id = it.getObjectId("_id").toString(),
            content = gson.toJson(JsonParser.parseString(it.filterKeys {
              it != "_id"
            }.json))
          )
        )
      }
    return result
  }

  suspend fun getOneDoc(collectionsEntity: CollectionsEntity, database: String, id: String): DocumentEntity {
    return DocumentEntity(
      _id = id,
      content = gson.toJson(
        JsonParser.parseString(
          mongoClient.getDatabase(database).getCollection(collectionsEntity.name).withKMongo()
            .find(eq(ObjectId(id))).first().awaitFirst().json
        )
      )
    )
  }
}