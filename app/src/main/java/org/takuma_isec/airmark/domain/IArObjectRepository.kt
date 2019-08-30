package org.takuma_isec.airmark.domain

interface IArObjectRepository {
    fun getArObject(srcUrl: String)
}