package com.example.azterketa.core

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    private const val SUPABASE_URL = "TU_SUPABASE_URL"
    private const val SUPABASE_ANON_KEY = "TU_SUPABASE_ANON_KEY"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(GoTrue)
        install(Postgrest)
    }
}