package com.azterketa.multimediaproyect.data.config

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    private const val SUPABASE_URL = "TU_SUPABASE_URL_AQUI"
    private const val SUPABASE_ANON_KEY = "TU_SUPABASE_ANON_KEY_AQUI"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}