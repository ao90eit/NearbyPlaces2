package com.aoinc.nearbyplaces2.util

class SearchConstants {
    companion object {
        const val ALL_SEARCH_PREFIX = "\"place of worship\"|church|mosque|synagogue|hindu|temple"
        const val BUDDHIST_SEARCH_KEYWORDS = "buddhist|buddha|buddhism"
        const val JAIN_SEARCH_KEYWORDS = "jain|jainist|jainism|derasar"
        const val OTHER_SEARCH_KEYWORDS = "shinto|jinja|bahai|sikh|gurdwara|tao|daoguan|pagan|\"cao dai\""

        // for icon checking
        val BUDDHIST_LIST_KEYWORDS = listOf("buddhist","buddha","buddhism")
        val JAIN_LIST_KEYWORDS = listOf("jain","jainist","jainism","derasar")
        val SHINTO_LIST_KEYWORDS = listOf("shinto","jinja","shintoism")
        val BAHAI_LIST_KEYWORDS = listOf("bahai")
        val SIKH_LIST_KEYWORDS = listOf("sikh","gurdwara","sikhism")
        val TAO_LIST_KEYWORDS = listOf("tao","daoguan","taoism","taoist")
        val PAGAN_LIST_KEYWORDS = listOf("pagan","paganist","paganism")
        val CAO_DAI_LIST_KEYWORDS = listOf("cao dai","caodaism")
    }
}