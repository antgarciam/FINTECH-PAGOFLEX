package cl.duocuc.miregistro.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val EsquemaClaro = lightColorScheme(
    primary = Primario,
    onPrimary = Blanco,
    primaryContainer = PrimarioClaro,
    onPrimaryContainer = PrimarioOscuro,

    secondary = Secundario,
    onSecondary = Blanco,
    secondaryContainer = PrimarioClaro,
    onSecondaryContainer = PrimarioOscuro,

    tertiary = Acento,
    onTertiary = TextoOscuro,

    background = FondoClaro,
    onBackground = TextoOscuro,

    surface = SuperficieClara,
    onSurface = TextoOscuro,

    surfaceVariant = PrimarioClaro,
    onSurfaceVariant = TextoOscuro,

    error = ColorError,
    onError = Blanco
)

private val EsquemaOscuro = darkColorScheme(
    primary = Primario,
    onPrimary = PrimarioOscuro,
    primaryContainer = PrimarioOscuro,
    onPrimaryContainer = PrimarioClaro,

    secondary = Secundario,
    onSecondary = Blanco,
    secondaryContainer = SuperficieOscura,
    onSecondaryContainer = TextoClaro,

    tertiary = Acento,
    onTertiary = FondoOscuro,

    background = FondoOscuro,
    onBackground = TextoClaro,

    surface = SuperficieOscura,
    onSurface = TextoClaro,

    surfaceVariant = SuperficieOscura,
    onSurfaceVariant = TextoClaro,

    error = ColorErrorOscuro,
    onError = Blanco
)

private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun MiRegistroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val esquemaColores = if (darkTheme) EsquemaOscuro else EsquemaClaro
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Pinta la barra de estado del color azul primario exacto del TopAppBar
            window.statusBarColor = esquemaColores.primary.toArgb()
            // Garantiza que los íconos de la barra (hora, wifi, batería) sean blancos/visibles
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = esquemaColores,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}