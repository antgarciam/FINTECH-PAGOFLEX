package cl.duocuc.miregistro.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme

@Composable
fun AvatarCircular(
    @DrawableRes imagen: Int,
    descripcion: String,
    modifier: Modifier = Modifier,
    // NUEVO (Avatar): foto elegida por el usuario. null = usar la imagen de res/drawable.
    // Con "= null" las llamadas antiguas a AvatarCircular siguen funcionando.
    foto: ImageBitmap? = null
) {
    // NUEVO (Avatar): Painter = "lo que sabe dibujarse" dentro de Image.
    //  - BitmapPainter    -> dibuja una imagen que está en memoria (la de la BD)
    //  - painterResource  -> dibuja una imagen de res/drawable (la de siempre)
    val painter = if (foto != null) BitmapPainter(foto) else painterResource(id = imagen)

    Image(
        painter = painter,
        contentDescription = descripcion,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(Dimens.tamanoAvatar)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .border(Dimens.bordeAvatar, MaterialTheme.colorScheme.surface, CircleShape)
    )
}

@Preview(showBackground = true)
@Composable
private fun AvatarCircularPreview() {
    MiRegistroTheme {
        AvatarCircular(imagen = R.drawable.avatar_usuario, descripcion = "", modifier = Modifier.padding(16.dp))
    }
}
