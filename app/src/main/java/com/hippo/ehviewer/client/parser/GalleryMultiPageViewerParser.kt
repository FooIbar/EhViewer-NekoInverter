/*
 * Copyright 2022 Tarsin Norbin
 *
 * This file is part of EhViewer
 *
 * EhViewer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * EhViewer is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with EhViewer.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package com.hippo.ehviewer.client.parser

import com.hippo.ehviewer.client.exception.ParseException
import org.json.JSONArray

object GalleryMultiPageViewerParser {
    private const val IMAGE_LIST_STRING = "var imagelist = "
    private val PATTERN_SHA1 = Regex("data-orghash=\"([^\"]+)\"")

    fun parsePToken(body: String): List<String> = runCatching {
        val index = body.indexOf(IMAGE_LIST_STRING)
        val imageList = body.substring(index + IMAGE_LIST_STRING.length, body.indexOf(";", index))
        val ja = JSONArray(imageList)
        (0 until ja.length()).map { ja.getJSONObject(it).getString("k") }
    }.getOrElse {
        throw ParseException("Parse pToken from MPV error", it)
    }

    fun parseSha1(body: String): List<String> = runCatching {
        PATTERN_SHA1.findAll(body).map { it.groupValues[1] }.toList()
    }.getOrElse {
        throw ParseException("Parse sha1 from MPV error", it)
    }
}
