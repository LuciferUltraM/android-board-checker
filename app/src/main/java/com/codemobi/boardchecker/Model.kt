package com.codemobi.boardchecker

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList


data class Worksheets(var worksheets: ArrayList<Worksheet>) {
    class Deserializer: ResponseDeserializable<Worksheets>{
        override fun deserialize(content: String): Worksheets? = Gson().fromJson(content, Worksheets::class.java)
    }
}

data class Model(var worksheet: Worksheet, var photos: ArrayList<Photo>) {
    class Deserializer: ResponseDeserializable<Model>{
        override fun deserialize(content: String): Model? = Gson().fromJson(content, Model::class.java)
    }
}

data class Worksheet(var id:Int, var number: String, var name: String, var created: Date)

data class Photo(var id:Int, var runningNumber: Int, var fileURL: String, var created: Date)