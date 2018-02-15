package com.kotlinBinaryHandler


/**
 * Created by SuperRainbowNinja on 8/01/2018.
 */

val EMPTY = Array(0, {0})

class Compiler(val lex: Lex) {
    constructor() : this(Lex())

    val types = HashMap<String, (name: String) -> IFactory>()

    init {
        types["int"] = ::IntFactory
        //types["char"] = ::CharSerializer
    }

    fun compile(source: String) =
            compile(source, false)

    fun compile(source: String, addToCompiler: Boolean) : ArrayList<CompoundSerializerFactory> {
        val AST = lex.parse(source)
        val compiledStruts: ArrayList<CompoundSerializerFactory> = ArrayList()

        //TODO error checks
        for (struct in AST.structs) {
            val nameMap = HashMap<String, IFactory>()
            val factoryList = Array(struct.property.size, {
                val property = struct.property[it]
                val fac = types[(property as ParsedProperty).type]?.invoke(property.name) ?: throw Exception("")
                if (property.value != null)
                    fac.setValue(property.value)
                nameMap[property.name] = fac
                fac
            })

            val tmp = CompoundSerializerFactory(struct.name, nameMap, struct.name, factoryList)
            if (addToCompiler)
                add(tmp.type, tmp::create)

            compiledStruts.add(
                    tmp
            )
        }
        return compiledStruts
    }

    fun add(type: String, constructor: (name: String) -> IFactory) {
        types[type] = constructor
    }
}

