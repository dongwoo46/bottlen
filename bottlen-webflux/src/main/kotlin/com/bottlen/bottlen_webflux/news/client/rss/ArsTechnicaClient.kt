package com.bottlen.bottlen_webflux.news.client.rss

import com.bottlen.bottlen_webflux.news.dto.rss.RssArticle
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.time.Instant
import java.time.format.DateTimeFormatter

@Component
class ArsTechnicaClient(
        webClientBuilder: WebClient.Builder
) : RssClient {

    private val sourceName = "ars_technica"

    private val webClient = webClientBuilder.build()

    override fun fetchArticles(url: String, topic: String): Flux<RssArticle> {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String::class.java)
                .flatMapMany { xml -> parse(xml, topic) }
    }

    override fun parse(xml: String, topic: String): Flux<RssArticle> {
        return Flux.create { sink ->
            try {
                val feed = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser())
                val entries = feed.select("item, entry")

                for (entry in entries) {

                    val title = entry.selectFirst("title")?.text()?.trim().orEmpty()
                    val link = extractLink(entry)
                    if (title.isBlank() || link.isBlank()) continue

                    val summaryRaw = entry.selectFirst("summary, description")?.html().orEmpty()
                    val summaryClean = cleanHtml(summaryRaw)

                    // HTML content (본문)
                    val contentRaw = extractContent(entry)
                    val contentClean = cleanHtml(contentRaw)

                    val publishedRaw = entry.selectFirst("published, pubDate")?.text().orEmpty()
                    val publishedIso = normalizeDate(publishedRaw)

                    val author = entry.selectFirst("author, dc\\:creator")?.text()?.trim()

                    val id = generateHashId(link, title)
                    val collectedAt = Instant.now().toString()

                    sink.next(
                            RssArticle(
                                    id = id,
                                    source = sourceName,
                                    topic = topic,
                                    title = title,
                                    link = link,
                                    summary = summaryClean,
                                    content = contentClean,
                                    published = publishedIso,
                                    author = author,
                                    lang = null,
                                    collectedAt = collectedAt
                            )
                    )
                }

                sink.complete()

            } catch (e: Exception) {
                sink.error(e)
            }
        }
    }

    // 본문 content 추출 (Atom + RSS)
    private fun extractContent(entry: Element): String {
        // RSS: <content:encoded>
        val rssContent = entry.selectFirst("content\\:encoded")?.html()
        if (!rssContent.isNullOrBlank()) return rssContent

        // Atom: <content type="html">
        val atomContent = entry.select("content[type=html]").html()
        if (atomContent.isNotBlank()) return atomContent

        return ""
    }

    // HTML 태그 제거
    private fun cleanHtml(raw: String): String {
        if (raw.isBlank()) return ""
        val text = Jsoup.parse(raw).text()
        return text.replace("\\s+".toRegex(), " ").trim()
    }

    // Atom <link href=""> vs RSS <link>
    private fun extractLink(entry: Element): String {
        val atomLink = entry.selectFirst("link[href]")
        return atomLink?.attr("href")
                ?: entry.selectFirst("link")?.text().orEmpty()
    }

    // pubDate → ISO 8601
    private fun normalizeDate(raw: String): String {
        if (raw.isBlank()) return Instant.now().toString()

        return try {
            val formatter = DateTimeFormatter.RFC_1123_DATE_TIME
            formatter.parse(raw, Instant::from).toString()
        } catch (_: Exception) {
            Instant.now().toString()
        }
    }

    // SHA-256 ID 생성 (link + title)
    private fun generateHashId(link: String, title: String): String {
        val input = "$link::$title"
        return java.security.MessageDigest
                .getInstance("SHA-256")
                .digest(input.toByteArray())
                .joinToString("") { "%02x".format(it) }
    }
}
