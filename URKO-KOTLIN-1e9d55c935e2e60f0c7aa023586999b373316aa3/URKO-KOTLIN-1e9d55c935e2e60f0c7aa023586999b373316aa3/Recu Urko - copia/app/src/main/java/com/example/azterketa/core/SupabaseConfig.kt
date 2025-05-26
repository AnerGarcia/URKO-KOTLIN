package com.example.azterketa.core

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    private const val SUPABASE_URL = "https://ufhqzcrgqudxzhunajwc.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVmaHF6Y3JncXVkeHpodW5handjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4MTI3MjcsImV4cCI6MjA2MzM4ODcyN30.vAnYeex14XtlWp8TyAk5jUFQcPcjAC-odnUBbgusj5U"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(GoTrue)
        install(Postgrest)
        // install(Realtime)  // si más adelante quieres realtime
        // install(Storage)   // si quieres storage
    }
}
