package com.kotlinBinaryHandler

/**
 * Created by SuperRainbowNinja on 19/01/2018.
 */
fun Array<Any>.asString() : String = String(CharArray(this.size, {this[it] as Char}))
fun Array<Char>.asString() : String = String(CharArray(this.size, this::get))