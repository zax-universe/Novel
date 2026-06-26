package com.azxuniverse.azxnov.data.remote

import com.azxuniverse.azxnov.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class NovelScraper {

    private val baseUrl = "https://sakuranovel.id"
    private val corsProxy = "https://cors.caliph.my.id/"
    private val httpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    private val requestHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
        "Accept-Language" to "id,en-US;q=0.7,en;q=0.3"
    )

    private suspend fun fetchDocument(url: String): Document = withContext(Dispatchers.IO) {
        val target = if (url.startsWith("http")) url else "$baseUrl$url"
        val request = Request.Builder()
            .url("$corsProxy$target")
            .apply { requestHeaders.forEach { (k, v) -> header(k, v) } }
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            Jsoup.parse(response.body?.string() ?: "", target)
        }
    }

    private suspend fun postDocument(url: String, params: Map<String, String>): Document = withContext(Dispatchers.IO) {
        val target = if (url.startsWith("http")) url else "$baseUrl$url"
        val body = FormBody.Builder().apply { params.forEach { (k, v) -> add(k, v) } }.build()

        val request = Request.Builder()
            .url("$corsProxy$target")
            .post(body)
            .apply { requestHeaders.forEach { (k, v) -> header(k, v) } }
            .header("X-Requested-With", "XMLHttpRequest")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            Jsoup.parse(response.body?.string() ?: "", target)
        }
    }

    suspend fun getHome(): HomeData {
        val doc = fetchDocument("/")

        val popular = doc.select(".flexbox-item").mapNotNull { el ->
            val title = el.select(".flexbox-title").text().trim()
            if (title.isEmpty()) return@mapNotNull null
            Novel(
                title = title,
                url = el.select("a").attr("href"),
                cover = el.select("img").attr("src").substringBefore("?")
            )
        }

        val latest = doc.select(".flexbox3-item").mapNotNull { el ->
            val title = el.select(".title a").text().trim()
            if (title.isEmpty()) return@mapNotNull null
            val latestChapterEl = el.select(".chapter li").first()
            Novel(
                title = title,
                url = el.selectFirst(".flexbox3-thumb img")?.parent()?.parent()?.attr("href") ?: "",
                cover = el.select(".flexbox3-thumb img").attr("src").substringBefore("?"),
                latestChapter = latestChapterEl?.let {
                    ChapterLink(
                        title = it.select("a").text().trim(),
                        url = it.select("a").attr("href"),
                        release = it.select(".date").text().trim()
                    )
                }
            )
        }

        return HomeData(popular = popular, latest = latest)
    }

    suspend fun search(query: String): List<Novel> {
        val doc = postDocument(
            "/wp-admin/admin-ajax.php",
            mapOf("action" to "data_fetch", "keyword" to query)
        )
        return doc.select(".searchbox").mapNotNull { el ->
            val url = el.select("a").attr("href")
            if (url.isEmpty()) return@mapNotNull null
            Novel(
                title = el.select(".searchbox-title").text().trim(),
                url = url,
                cover = el.select(".searchbox-thumb img").attr("src").substringBefore("?"),
                type = el.select(".type").text().trim(),
                status = el.select(".status").text().trim()
            )
        }
    }

    suspend fun getDetail(url: String): Novel {
        val doc = fetchDocument(url)
        val title = doc.select(".series-titlex h2").text().trim()
        if (title.isEmpty()) throw Exception("Novel not found")

        val infoMap = buildMap {
            doc.select(".series-infolist li").forEach { el ->
                val key = el.select("b").text().replace(":", "").trim().lowercase()
                val value = el.text().replace(el.select("b").text(), "").trim()
                put(key, value)
            }
        }

        val chapters = doc.select(".series-chapterlist li").map { el ->
            val anchor = el.select(".flexch-infoz a")
            val rawTitle = anchor.select("span").first()?.text() ?: ""
            ChapterLink(
                title = rawTitle.replace(Regex("\\s+"), " ")
                    .replace(Regex(" Bahasa Indonesia$", RegexOption.IGNORE_CASE), "")
                    .trim(),
                url = anchor.attr("href"),
                release = el.select(".date").text().trim()
            )
        }

        return Novel(
            title = title,
            url = url,
            cover = doc.select("meta[property=og:image]").attr("content")
                .ifEmpty { doc.select(".series-thumb img").attr("src") },
            synopsis = doc.select(".series-synops").text().trim(),
            metadata = NovelMetadata(
                type = doc.select(".series-infoz.block .type").text().trim(),
                status = doc.select(".series-infoz.block .status").text().trim(),
                rating = doc.select(".series-infoz.score span[itemprop=ratingValue]").text().trim(),
                bookmarks = doc.select(".favcount span").text().trim(),
                country = infoMap["country"],
                published = infoMap["published"],
                author = infoMap["author"],
                lastUpdated = doc.select("meta[property=og:updated_time]").attr("content")
            ),
            genres = doc.select(".series-genres a").map { it.text().trim() },
            tags = doc.select(".series-infolist li:contains(Tags) a").map { it.text().trim() },
            chapters = chapters
        )
    }

    suspend fun getChapter(url: String): ChapterContent {
        val doc = fetchDocument(url)
        val rawTitle = doc.select(".title-chapter").text().trim()
        if (rawTitle.isEmpty()) throw Exception("Chapter not found")

        val contentArea = doc.select(".tldariinggrissendiribrojangancopy")

        val images = contentArea.select("img").mapNotNull { el ->
            el.attr("src").ifEmpty { el.attr("data-src") }
                .ifEmpty { el.attr("srcset").split(" ").firstOrNull() }
                ?.substringBefore("?")
        }

        val textLines = contentArea.select("p").mapNotNull { el ->
            val text = el.text().trim()
            if (text.isEmpty()) return@mapNotNull null
            if (text.contains("Baca novel lain", ignoreCase = true)) return@mapNotNull null
            text
        }

        return ChapterContent(
            title = rawTitle.replace(Regex(" Bahasa Indonesia$", RegexOption.IGNORE_CASE), "").trim(),
            fullTitle = rawTitle,
            contentText = textLines.joinToString("\n\n"),
            images = images,
            navigation = Navigation(
                prev = doc.select(".pagi-prev a").attr("href").ifEmpty { null },
                toc = doc.select(".pagi-toc a").attr("href").ifEmpty { null },
                next = doc.select(".pagi-next a").attr("href").ifEmpty { null }
            )
        )
    }

    suspend fun getGenres(): List<Genre> {
        val doc = fetchDocument("/genre/")
        return doc.select(".achlist li a").map { el ->
            Genre(
                name = el.ownText().trim(),
                count = el.select("span").text().trim(),
                url = el.attr("href")
            )
        }
    }
}
