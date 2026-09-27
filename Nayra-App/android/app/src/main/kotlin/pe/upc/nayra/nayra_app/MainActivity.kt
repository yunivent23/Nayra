package pe.upc.nayra.nayra_app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec

/**
 * Canal de plataforma para la clave del dispositivo vinculado (D-048, D-007).
 *
 * - Par EC P-256 generado dentro del Android Keystore: la clave privada nunca sale del
 *   almacén ni llega a Dart; solo se exportan la clave pública y las firmas.
 * - Firma SHA256withECDSA del mensaje "nonce|dispositivoId|proposito" que envía el backend.
 * - PROVISIONAL: sin exigir autenticación del usuario para usar la clave ni StrongBox;
 *   el endurecimiento del almacén queda para las decisiones de seguridad pendientes.
 */
class MainActivity : FlutterActivity() {

    private companion object {
        const val CANAL = "pe.upc.nayra/dispositivo"
        const val ALIAS = "nayra_dispositivo_v1"
        const val PREFERENCIAS = "nayra_prototipo"
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CANAL).setMethodCallHandler { llamada, resultado ->
            try {
                when (llamada.method) {
                    "clavePublica" -> resultado.success(clavePublica())
                    "firmar" -> {
                        val mensaje = Base64.decode(llamada.argument<String>("mensaje"), Base64.NO_WRAP)
                        resultado.success(firmar(mensaje))
                    }
                    "leerIdentificadores" -> {
                        val p = getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                        val ids = mutableMapOf<String, String>()
                        p.getString("cuentaId", null)?.let { ids["cuentaId"] = it }
                        p.getString("dispositivoId", null)?.let { ids["dispositivoId"] = it }
                        resultado.success(ids)
                    }
                    "guardarIdentificadores" -> {
                        getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).edit()
                            .putString("cuentaId", llamada.argument<String>("cuentaId"))
                            .putString("dispositivoId", llamada.argument<String>("dispositivoId"))
                            .apply()
                        resultado.success(null)
                    }
                    else -> resultado.notImplemented()
                }
            } catch (e: Exception) {
                // Mensaje genérico: no se exponen detalles del almacén de claves.
                resultado.error("CLAVE_DISPOSITIVO", "Error de la clave del dispositivo.", null)
            }
        }
    }

    private fun almacen(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun clavePublica(): String {
        val ks = almacen()
        if (!ks.containsAlias(ALIAS)) {
            val generador = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
            generador.initialize(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN)
                    .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .build()
            )
            generador.generateKeyPair()
        }
        return Base64.encodeToString(ks.getCertificate(ALIAS).publicKey.encoded, Base64.NO_WRAP)
    }

    private fun firmar(mensaje: ByteArray): String {
        val privada = almacen().getKey(ALIAS, null) as PrivateKey
        val firma = Signature.getInstance("SHA256withECDSA").apply {
            initSign(privada)
            update(mensaje)
        }.sign()
        return Base64.encodeToString(firma, Base64.NO_WRAP)
    }
}
