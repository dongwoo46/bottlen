package com.bottlen.bottlen_webflux.infra.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "external")
class ExternalProperties {
    lateinit var paper: PaperProperties
    lateinit var contact: ContactProperties
    lateinit var news: NewsProperties
    lateinit var social: SocialProperties
}

class ContactProperties {
    lateinit var mailto: String
}

class PaperProperties {
    lateinit var openalex: OpenAlex
    lateinit var core: Core
    lateinit var crossref: Crossref
    lateinit var opencitations: OpenCitations

    class OpenAlex {
        lateinit var baseUrl: String
        lateinit var mailto: String
    }

    class Core {
        lateinit var baseUrl: String
        lateinit var apiKey: String
    }

    class Crossref {
        lateinit var baseUrl: String
    }

    class OpenCitations {
        lateinit var baseUrl: String
    }
}

class NewsProperties {
    lateinit var guardian: ApiKeyBaseUrl
    lateinit var newsdata: ApiKeyBaseUrl
    lateinit var newscatcher: ApiKeyBaseUrl
    lateinit var newyorktimes: ApiKeyBaseUrl
    lateinit var gdelt: Gdelt

    class ApiKeyBaseUrl {
        lateinit var baseUrl: String
        lateinit var apiKey: String
    }

    class Gdelt {
        lateinit var docBaseUrl: String
        lateinit var contextBaseUrl: String
    }
}

class SocialProperties {
    lateinit var reddit: Reddit

    class Reddit {
        lateinit var authBaseUrl: String
        lateinit var apiBaseUrl: String
        lateinit var clientId: String
        lateinit var clientSecret: String
    }
}

