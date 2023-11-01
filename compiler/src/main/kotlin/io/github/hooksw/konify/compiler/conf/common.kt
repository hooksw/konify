package io.github.hooksw.konify.compiler.conf

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

const val packageName="io.github.hooksw.konify.runtime"

fun classIdFor(pname:String,cname:String)=
    ClassId(FqName("$packageName.$pname"), Name.identifier(cname))
fun callableIdFor(pname:String,cname:String?=null,fname:String)=
    CallableId(FqName("$packageName.$pname"),cname?.let { FqName(it) }, Name.identifier(fname))
