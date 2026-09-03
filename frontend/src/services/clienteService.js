export async function consultarClientes(token, texto = '') {

    const parametros = new URLSearchParams({
        texto: texto.trim(),
    })
    const respuesta = await fetch(
        `/api/clientes/busqueda?${parametros.toString()}`, {
        method: 'GET',
        headers: {
            Authorization: `Bearer ${token}`,
        },
    })

    if (!respuesta.ok) {
        if (respuesta.status === 401) {
            throw new Error('La sesión no es válida o expiró')
        }

        if (respuesta.status === 403) {
            throw new Error('No tenés permisos para consultar clientes')
        }

        throw new Error('No se pudieron consultar los clientes')
    }

    return respuesta.json()
}