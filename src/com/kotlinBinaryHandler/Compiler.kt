package com.kotlinBinaryHandler

/**
 * Created by SuperRainbowNinja on 8/01/2018.
 */

val EMPTY = Array(0, {0})

class Compiler(val lex: Lex) {
    constructor() : this(Lex())

    val types = HashMap<String, (name: String) -> IDependentSerializer>()

    init {
        types["int"] = ::IntSerializer
        types["char"] = ::CharSerializer
    }

    fun compile(source: String) : CustomSerializer {
        val AST = lex.parse(source)
        val nameMap = HashMap<String, Int>()
        val factoryList = Array<IDependentSerializer?>(AST.structs[0].property.size, { null })

        //TODO error checks
        for (struct in AST.structs) {
            for ((i, property) in struct.property.withIndex()) {
                when (property) {
                    is ParsedProperty -> {
                        nameMap[property.name] = i
                        val fac = types[property.type]
                        if (fac != null)
                            factoryList[i] = fac(property.name)
                        else
                            throw Exception("no type")
                    }
                    is ParsedIndexProperty -> {
                        nameMap[property.name] = i


                        val num = property.indexExp.toIntOrNull()
                        val lengthGetter: ILengthProxy
                        if (num == null) {
                            val ref = nameMap[property.indexExp] as Int
                            factoryList[ref] = (factoryList[ref] as LengthProxyProvider).getProxy()
                            lengthGetter = factoryList[ref] as ILengthProxy
                        } else {
                            lengthGetter = object : ILengthProxy, Constant {
                                override var length: Int
                                    get() = num
                                    set(value) {
                                        if (num != value) throw Exception("Tried to serialize array with wrong length")
                                    }

                            }
                        }




                        val fac = types[property.type]
                        if (fac != null)
                            factoryList[i] = VarArraySerializer(property.name, fac("") as IIndependentSerializer, lengthGetter)
                        else
                            throw Exception("no type")

                    }
                    is ParsedMagicNumber -> {
                        nameMap[property.name] = i
                        val fac = types[property.type]
                        if (fac != null) {
                            if (property.array == null) {
                                val factory = fac(property.name)
                                factoryList[i] = factory.with(property.value)
                            } else {
                                val length = property.array.toIntOrNull() ?: throw Exception("Cannot use variable array with constant values")
                                val lengthGetter = object : ILengthProxy, Constant {
                                    override var length: Int
                                        get() = length
                                        set(value) {
                                            if (length != value) throw Exception("Tried to serialize array with wrong length")
                                        }

                                }
                                factoryList[i] = VarArraySerializer(property.name, fac("") as IIndependentSerializer, lengthGetter)
                                        .with(property.value)
                            }
                        } else {
                            throw Exception("no type")
                        }
                    }
                }
            }
        }
        return CustomSerializer(AST.structs[0].name ,Array(factoryList.size, { factoryList.get(it) ?: throw Exception("Unresonable exception")}))
    }
}

