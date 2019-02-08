//package com.macgavrina.co_accounting.room
//
//import android.os.Build
//import androidx.room.TypeConverter
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//import com.macgavrina.co_accounting.logging.Log
//import java.util.*
//import java.util.stream.Collectors
//
//
//class ListTypeConverter {
//
//
////    @TypeConverter
////    fun listToJson(value: List<String>?): String? {
////
////        Log.d("listToJson: ${Gson().toJson(value)}")
////
////        if (value == null) return null
////        return Gson().toJson(value)
////    }
////
////    @TypeConverter
////    fun jsonToList(value: String?): List<String>? {
////
////        if (value == null) return null
////
//////        val list: List<String> = Gson().fromJson(value, Array<String>::class.java).toList()
////
////        val objects = Gson().fromJson(value, Array<String>::class.java) as Array<String>
////
////        val list = objects.toList()
////
////        Log.d("jsonToList: $list")
////
////        return list
////    }
//
//    @TypeConverter
//    fun fromListToString(list: List<String>?): String? {
//        if (list == null || list.isEmpty()) return null
//
////        var listString: String = list[0]
////
////
////        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
////            list.stream().collect(Collectors.joining(","))
////        } else {
////            TODO("VERSION.SDK_INT < N")
////        }
//
//        return "0"
//    }
//
//    @TypeConverter
//    fun fromStringToList(listString: String?): List<String>? {
//        if (listString == null) return null
//        return listString.split(",".toRegex())
//    }
//}