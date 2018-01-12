package com.kotlinBinaryHandler.backup

import com.kotlinBinaryHandler.*
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by SuperRainbowNinja on 11/01/2018.
 */

/*
class Compiler(val lex: Lex) {
    constructor() : this(Lex())

    val types = HashMap<String, (name: String) -> ISerializerFactory<*>>()

    init {
        types["int"] = ::IntSerializerFactory
    }

    fun compile(source: String) : SerializeHandlerFactory {
        val AST = lex.parse(source)
        val nameMap = HashMap<String, Int>()
        val factoryList = Array<ISerializerFactory<*>?>(AST.structs[0].property.size, { null })

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
                        val ref = nameMap[property.indexExp]
                        if (ref != null) {
                            factoryList[ref] = LengthSerializerFactory(factoryList[ref]?.name ?: throw Exception("gotta fix this shit"), i)
                            val fac = types[property.type]
                            if (fac != null)
                                factoryList[i] = ArraySerializerFactory(property.name, fac(""), ref)
                            else
                                throw Exception("no type")
                        }
                    }
                    is ParsedMagicNumber -> {
                        nameMap[property.name] = i
                        //factoryList[i] = MagicNumberFactory(property.name, property.value.toInt())
                    }
                }
            }
        }
        return SerializeHandlerFactory(AST.structs[0].name, Array(factoryList.size, { factoryList.get(it) ?: throw Exception("Unresonable exception")}))
    }
}

fun getSerializer(sh : SerializeHandlerFactory, v : Array<Any?>) : Array<IBinarySerializer> {
    val array : Array<IBinarySerializer> = Array(sh.data.size, {sh.data[it].getSerializer(v[it])})
    for ((i, property) in array.withIndex()) {
        (sh.data[i] as ISerializerFactory<IBinarySerializer>).resolveDependency(property, array)
    }
    return array
}

interface ISerializerFactory<T : IBinarySerializer> {
    val name: String
    fun getSerializer(value : Any?) : T
    fun getDeserializer() : T
    fun resolveDependency(s : T, others : Array<IBinarySerializer>)
}

interface IBinarySerializer {
    val value: Any?
    val factory: ISerializerFactory<*>
    fun serialize(stream: OutputStream)
    fun deserialize(stream: InputStream)
}


class LengthSerializerFactory(override val name: String, val ref : Int) : ISerializerFactory<LengthSerializerFactory.LengthSerializer> {
    override fun getSerializer(value: Any?): LengthSerializer = LengthSerializer()

    override fun getDeserializer(): LengthSerializer = LengthSerializer()

    override fun resolveDependency(s: LengthSerializer, others: Array<IBinarySerializer>) {
        s.arrayRef = others[ref] as ArraySerializerFactory.ArraySerializer
    }
    inner class LengthSerializer : IBinarySerializer {
        var arrayRef: ArraySerializerFactory.ArraySerializer? = null

        override val factory: ISerializerFactory<*>
            get() = this@LengthSerializerFactory

        override var value: Int = 0

        override fun deserialize(stream: InputStream) {
            value = getInt(stream)
        }

        override fun serialize(stream: OutputStream) {
            value = (arrayRef?.value as Array<*>).size
            serInt(stream, value)
        }
    }
}



class ArraySerializerFactory(override val name: String, val typeFact : ISerializerFactory<*>, val ref : Int) : ISerializerFactory<ArraySerializerFactory.ArraySerializer> {
    override fun getSerializer(value: Any?): ArraySerializer = ArraySerializer(typeFact, value as Array<*>)

    override fun getDeserializer(): ArraySerializer = ArraySerializer(typeFact, EMPTY)

    override fun resolveDependency(s: ArraySerializer, others: Array<IBinarySerializer>) {
        s.lengthRef = others[ref] as LengthSerializerFactory.LengthSerializer
    }

    inner class ArraySerializer(val typeFact : ISerializerFactory<*>, var array: Array<*>) : IBinarySerializer {
        override val value: Any?
            get() = array

        override val factory: ISerializerFactory<*>
            get() = this@ArraySerializerFactory

        var lengthRef: LengthSerializerFactory.LengthSerializer? = null

        override fun serialize(stream: OutputStream) {
            for (value in array) {
                typeFact.getSerializer(value).serialize(stream)
            }
        }

        override fun deserialize(stream: InputStream) {
            val getter = typeFact.getDeserializer()
            array = Array<Any?>(lengthRef?.value as Int, {
                getter.deserialize(stream)
                getter.value
            })
        }
    }
}



class IntSerializerFactory(override val name: String) : ISerializerFactory<IntSerializerFactory.IntSerializer> {

    override fun getSerializer(value: Any?): IntSerializer = IntSerializer(value as Int)

    override fun getDeserializer(): IntSerializer = IntSerializer(0)

    override fun resolveDependency(s: IntSerializer, others: Array<IBinarySerializer>) {}

    inner class IntSerializer(var num: Int) : IBinarySerializer {
        override val value: Any? get() = num

        override val factory: ISerializerFactory<*>
            get() = this@IntSerializerFactory

        override fun serialize(stream: OutputStream) {
            serInt(stream, num)
        }

        override fun deserialize(stream: InputStream) {
            num = getInt(stream)
        }
    }
}

class MagicNumberFactory(override val name: String, val value: Int) : ISerializerFactory<MagicNumberFactory.MagicNumberSerializer> {
    override fun getSerializer(value: Any?): MagicNumberSerializer = MagicNumberSerializer(this.value)

    override fun getDeserializer(): MagicNumberSerializer = MagicNumberSerializer(value)

    override fun resolveDependency(s: MagicNumberSerializer, others: Array<IBinarySerializer>) {}

    inner class MagicNumberSerializer(val num: Int) : IBinarySerializer {
        override val value: Any?
            get() = num
        override val factory: ISerializerFactory<*>
            get() = this@MagicNumberFactory

        override fun serialize(stream: OutputStream) {
            serInt(stream, num)
        }

        override fun deserialize(stream: InputStream) {
            val input = getInt(stream)
            if (input != num) {
                throw Exception("Magic number $num mot found")
            }
        }

    }

}


class SerializeHandlerFactory(override val name: String, val data: Array<ISerializerFactory<*>>) : ISerializerFactory<SerializeHandlerFactory.SerializeHandler> {
    override fun getSerializer(value: Any?): SerializeHandler = SerializeHandler(Array(data.size, { data[it].getSerializer((value as Array<Any?>)[it]) }), value as Array<Any?>)

    override fun getDeserializer(): SerializeHandler = SerializeHandler(Array(data.size, { data[it].getDeserializer() }), Array(data.size, {null}))

    override fun resolveDependency(s: SerializeHandler, others: Array<IBinarySerializer>) {
        for ((i, factory) in (data as Array<ISerializerFactory<IBinarySerializer>>).withIndex()) {
            factory.resolveDependency(s.data[i], s.data)
        }
    }

    fun serialize(stream: OutputStream, arr: Array<Any?>) {
        val ser = getSerializer(arr)
        resolveDependency(ser, ser.data)
        ser.serialize(stream)
    }

    fun deserialize(stream: InputStream) : Array<Any?> {
        val ser = getDeserializer()
        resolveDependency(ser, ser.data)
        ser.deserialize(stream)
        return ser.dataStash
    }


    inner class SerializeHandler(val data: Array<IBinarySerializer>, var dataStash: Array<Any?>) : IBinarySerializer {
        override val factory: ISerializerFactory<*>
            get() = this@SerializeHandlerFactory

        override val value: Any?
            get() = dataStash

        override fun serialize(stream: OutputStream) {
            data.forEach { it.serialize(stream) }
        }

        override fun deserialize(stream: InputStream) {
            for ((i, property) in data.withIndex()) {
                property.deserialize(stream)
                dataStash[i] = property.value
            }
        }
    }
}

*/