package com.bottlen.bottlen_webflux.infra.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "external")
data class ExternalProperties(
    val paper: PaperProperties = PaperProperties(),
    val contact: ContactProperties = ContactProperties(),
    val news: NewsProperties = NewsProperties(),
    val social: SocialProperties = SocialProperties()
)

/* =========================
 * Contact
 * ========================= */
data class ContactProperties(
    val mailto: String = ""
)

/* =========================
 * Paper / Academic APIs
 * ========================= */
data class PaperProperties(
    val openalex: OpenAlexProperties = OpenAlexProperties(),
    val core: CoreProperties = CoreProperties(),
    val crossref: CrossrefProperties = CrossrefProperties(),
    val opencitations: OpenCitationsProperties = OpenCitationsProperties()
)

data class OpenAlexProperties(
    val baseUrl: String = ""
)

data class CoreProperties(
    val baseUrl: String = "",
    val apiKey: String = ""
)

data class CrossrefProperties(
    val baseUrl: String = ""
)

data class OpenCitationsProperties(
    val baseUrl: String = ""
)

/* =========================
 * News APIs
 * ========================= */
data class NewsProperties(
    val guardian: ApiKeyBaseUrlProperties = ApiKeyBaseUrlProperties(),
    val newsdata: ApiKeyBaseUrlProperties = ApiKeyBaseUrlProperties(),
    val newscatcher: ApiKeyBaseUrlProperties = ApiKeyBaseUrlProperties(),
    val newyorktimes: ApiKeyBaseUrlProperties = ApiKeyBaseUrlProperties(),
    val gdelt: GdeltProperties = GdeltProperties()
)

data class ApiKeyBaseUrlProperties(
    val baseUrl: String = "",
    val apiKey: String = ""
)

data class GdeltProperties(
    val docBaseUrl: String = "",
    val contextBaseUrl: String = ""
)

/* =========================
 * Social APIs
 * ========================= */
data class SocialProperties(
    val reddit: RedditProperties = RedditProperties()
)

data class RedditProperties(
    val authBaseUrl: String = "",
    val apiBaseUrl: String = "",
    val clientId: String = "",
    val clientSecret: String = ""
)
