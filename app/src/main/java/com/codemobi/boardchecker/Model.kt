package com.codemobi.boardchecker

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList


data class Projects(var projects: ArrayList<Project>) {
    class Deserializer: ResponseDeserializable<Projects>{
        override fun deserialize(content: String): Projects? = Gson().fromJson(content, Projects::class.java)
    }
}

data class Model(var project: Project, var photos: ArrayList<Photo>) {
    class Deserializer: ResponseDeserializable<Model>{
        override fun deserialize(content: String): Model? = Gson().fromJson(content, Model::class.java)
    }
}

data class Project(var id:Int, var name: String, var fileURL: String, var created: Date)

data class Photo(var id:Int, var runningNumber: Int, var fileURL: String, var created: Date)