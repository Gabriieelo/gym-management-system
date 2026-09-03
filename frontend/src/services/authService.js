export async function iniciarSesion(nombreUsuario, password) {
  const respuesta = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      nombreUsuario,
      password,
    }),
  })

  if (!respuesta.ok) {
    if (respuesta.status === 401) {
      throw new Error('Usuario o contraseña incorrectos')
    }

    throw new Error('No se pudo iniciar sesión')
  }

  const datos = await respuesta.json()
  return datos
}