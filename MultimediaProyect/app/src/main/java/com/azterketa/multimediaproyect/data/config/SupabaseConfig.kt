package com.azterketa.multimediaproyect.data.config

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    // TODO: Reemplaza con tus credenciales reales de Supabase
    private const val SUPABASE_URL = "https://tu-proyecto.supabase.co"
    private const val SUPABASE_ANON_KEY = "tu-clave-anonima-aqui"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}